package com.yangshengzhou.gobang.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangshengzhou.gobang.game.*;
import com.yangshengzhou.gobang.entity.User;
import com.yangshengzhou.gobang.entity.Room;
import com.yangshengzhou.gobang.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.Map;

@Component
public class GameController extends TextWebSocketHandler {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Resource
    private UserMapper userMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        GameReadyResponse resp = new GameReadyResponse();

        User user = (User) session.getAttributes().get("user");
        if(user == null){
            resp.setOk(false);
            resp.setMessage("用户尚未登录");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        Room room = roomManager.getRoomByUserId(user.getUserId());
        if(room == null){
            resp.setOk(false);
            resp.setReason("用户尚未匹配到！");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        if(onlineUserManager.getFromGameHall(user.getUserId()) != null
                || onlineUserManager.getFromGameRoom(user.getUserId()) != null){
            resp.setOk(true);
            resp.setReason("禁止多开游戏页面");
            resp.setMessage("repeatConnection");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(resp)));
            return;
        }

        onlineUserManager.enterGameRoom(user.getUserId(), session);

        synchronized (room){
            if(room.getUser1() == null){
                room.setUser1(user);
                room.setWhiteUser(user);
                System.out.println("玩家 "+user.getUsername() + "已经准备就绪");
                return;
            }
            if(room.getUser2() == null){
                room.setUser2(user);
                System.out.println("玩家 "+user.getUsername() + "已经准备就绪");

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
        resp.setWhiteUser(room.getWhiteUser().getUserId());
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
        if(user == null){
            System.out.println("[handleTextMessage] 当前用户尚未登录");
            return;
        }
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if(room == null){
            return;
        }
        room.putChess(message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if(user == null){
            return;
        }
        WebSocketSession exitSession = onlineUserManager.getFromGameRoom(user.getUserId());
        if(session==exitSession){
            onlineUserManager.exitGameRoom(user.getUserId());
        }
        System.out.println("当前用户 "+user.getUsername() + " 游戏房间连接异常");
        noticeThatUserWin(user);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        User user = (User) session.getAttributes().get("user");
        if(user == null){
            return;
        }
        WebSocketSession exitSession = onlineUserManager.getFromGameRoom(user.getUserId());
        if(session==exitSession){
            onlineUserManager.exitGameRoom(user.getUserId());
        }
        System.out.println("当前用户 "+user.getUsername() + " 离开游戏房间");
        noticeThatUserWin(user);
    }

    private void noticeThatUserWin(User user) throws IOException {
        Room room = roomManager.getRoomByUserId(user.getUserId());
        if(room == null){
            System.out.println("用户 "+user.getUsername()+" 已经不在房间中");
            return;
        }
        User thatUser = (user == room.getUser1()) ? room.getUser2() : room.getUser1();
        if(thatUser == null){
            System.out.println("在房间 "+room.getRoomId()+" 中找不到对手");
            return;
        }
        WebSocketSession webSocketSession = onlineUserManager.getFromGameRoom(thatUser.getUserId());
        if(webSocketSession == null){
            System.out.println("对手 "+thatUser.getUsername()+" 不在线");
            return;
        }
        GameResponse response = new GameResponse();
        response.setMessage("putChess");
        response.setUserId(thatUser.getUserId());
        response.setWinner(thatUser.getUserId());
        webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        int winUserId = thatUser.getUserId();
        int loseUserId = user.getUserId();
        userMapper.userWin(winUserId);
        userMapper.userLose(loseUserId);
        roomManager.remove(room,room.getUser1().getUserId(),room.getUser2().getUserId());
    }
}