package com.yangshengzhou.gobang.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangshengzhou.gobang.entity.Room;
import com.yangshengzhou.gobang.entity.User;
import com.yangshengzhou.gobang.game.GameReadyResponse;
import com.yangshengzhou.gobang.game.GameResponse;
import com.yangshengzhou.gobang.game.OnlineUserManager;
import com.yangshengzhou.gobang.game.RoomManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * 对局房间的 WebSocket 入口（/game）。
 *
 * 负责：把两个玩家安排进同一个 Room、把落子请求转给 Room、在掉线时判负。
 * 具体的棋盘规则都在 Room 里实现。
 */
@Component
public class GameController extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        GameReadyResponse resp = new GameReadyResponse();

        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            resp.setOk(false);
            resp.setMessage("用户尚未登录");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        Room room = roomManager.getRoomByUserId(user.getUserId());
        if (room == null) {
            resp.setOk(false);
            resp.setReason("用户尚未匹配到！");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        // 进入对局时先把大厅里的残留连接清掉：匹配成功后前端可能还没来得及关闭匹配连接，
        // 旧逻辑会因此误判为“多开”而拒绝进入房间。
        if (onlineUserManager.getFromGameHall(user.getUserId()) != null) {
            onlineUserManager.exitGameHall(user.getUserId());
        }

        if (onlineUserManager.getFromGameRoom(user.getUserId()) != null) {
            resp.setOk(true);
            resp.setReason("禁止多开游戏页面");
            resp.setMessage("repeatConnection");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        onlineUserManager.enterGameRoom(user.getUserId(), session);

        synchronized (room) {
            if (room.getUser1() == null) {
                room.setUser1(user);
                room.setWhiteUser(user);
                System.out.println("玩家 " + user.getUsername() + " 已经准备就绪（先手）");
                return;
            }
            if (room.getUser2() == null && room.getUser1().getUserId() != user.getUserId()) {
                room.setUser2(user);
                room.ensureGame();
                System.out.println("玩家 " + user.getUsername() + " 已经准备就绪");

                noticeGameReady(room, room.getUser1(), room.getUser2());
                noticeGameReady(room, room.getUser2(), room.getUser1());
                return;
            }
        }

        resp.setOk(false);
        resp.setReason("当前房间已满，您不能加入房间");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
    }

    private void noticeGameReady(Room room, User thisUser, User thatUser) throws Exception {
        GameReadyResponse resp = new GameReadyResponse();
        resp.setMessage("gameReady");
        resp.setOk(true);
        resp.setReason("");
        resp.setRoomId(room.getRoomId());
        resp.setThisUserId(thisUser.getUserId());
        resp.setThatUserId(thatUser.getUserId());
        resp.setThatUsername(thatUser.getUsername());
        resp.setWhiteUser(room.getWhiteUser() == null ? 0 : room.getWhiteUser().getUserId());
        resp.setGameType(room.getGameType());
        resp.setState(room.getInitialState());
        WebSocketSession webSocketSession = onlineUserManager.getFromGameRoom(thisUser.getUserId());
        if (webSocketSession != null && webSocketSession.isOpen()) {
            webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
        } else {
            System.err.println("用户 " + thisUser.getUsername() + " 的游戏房间连接未建立");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            System.out.println("[handleTextMessage] 当前用户尚未登录");
            return;
        }
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if (room == null) {
            sendRoomError(session, "房间不存在或对局已结束");
            return;
        }

        // 客户端消息一律不能让异常冒到容器：否则 Spring 会直接以 1011 关闭整条连接
        try {
            String payload = message.getPayload();
            String type = null;
            try {
                type = objectMapper.readTree(payload).path("message").asText(null);
            } catch (Exception ignored) {
                // 交给 Room 去解析并返回错误
            }
            if ("surrender".equals(type)) {
                room.surrender(user);
                return;
            }
            room.putChess(user, payload);
        } catch (Exception e) {
            System.err.println("[GameController] 处理消息失败: " + e.getMessage());
            sendRoomError(session, "非法的请求");
        }
    }

    private void sendRoomError(WebSocketSession session, String reason) throws IOException {
        GameResponse resp = new GameResponse();
        resp.setMessage("error");
        resp.setReason(reason);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        WebSocketSession exitSession = onlineUserManager.getFromGameRoom(user.getUserId());
        if (session == exitSession) {
            onlineUserManager.exitGameRoom(user.getUserId());
        }
        System.out.println("当前用户 " + user.getUsername() + " 游戏房间连接异常");
        handlePlayerLeft(user);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if (user == null) {
            return;
        }
        WebSocketSession exitSession = onlineUserManager.getFromGameRoom(user.getUserId());
        if (session == exitSession) {
            onlineUserManager.exitGameRoom(user.getUserId());
        }
        System.out.println("当前用户 " + user.getUsername() + " 离开游戏房间");
        handlePlayerLeft(user);
    }

    /** 玩家离开：房间还在就判对手获胜，房间已销毁则什么都不做 */
    private void handlePlayerLeft(User user) {
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if (room == null) {
            System.out.println("用户 " + user.getUsername() + " 已经不在房间中");
            return;
        }
        room.playerLeft(user);
    }
}
