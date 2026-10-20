package com.yangshengzhou.gobang.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangshengzhou.gobang.GobangApplication;
import com.yangshengzhou.gobang.game.GameResponse;
import com.yangshengzhou.gobang.game.OnlineUserManager;
import com.yangshengzhou.gobang.game.RoomManager;
import com.yangshengzhou.gobang.game.games.Game;
import com.yangshengzhou.gobang.game.games.GameFactory;
import com.yangshengzhou.gobang.mapper.UserMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 一个对局房间：负责玩家、连接、结算与消息广播；具体棋规交给 {@link Game} 实现。
 *
 * user1 恒为主队（五子棋执黑先手 / 象棋执红 / 军棋执红），user2 为客队。
 */
public class Room {

    private String roomId;
    private String gameType = GameFactory.GOBANG;

    private User user1;
    private User user2;

    /** 先手玩家（五子棋里的黑棋） */
    private User whiteUser;

    private final OnlineUserManager onlineUserManager;
    private final RoomManager roomManager;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Game game;

    private boolean finished = false;
    private final long startTime = System.currentTimeMillis();

    public Room() {
        roomId = UUID.randomUUID().toString();
        onlineUserManager = GobangApplication.context.getBean(OnlineUserManager.class);
        roomManager = GobangApplication.context.getBean(RoomManager.class);
        userMapper = GobangApplication.context.getBean(UserMapper.class);
    }

    // ================= 开局 =================

    /** 双方都到齐后创建规则引擎（幂等） */
    public synchronized void ensureGame() {
        if (game == null && user1 != null && user2 != null) {
            game = GameFactory.create(gameType, user1, user2);
        }
    }

    public synchronized Map<String, Object> getInitialState() {
        ensureGame();
        return game == null ? new LinkedHashMap<>() : game.initialState();
    }

    public synchronized int getTurnUserId() {
        return game == null ? 0 : game.getTurnUserId();
    }

    // ================= 走子 =================

    public synchronized void putChess(User player, String reqJson) throws IOException {
        if (player == null) {
            return;
        }
        if (finished) {
            sendError(player, "本局已经结束");
            return;
        }
        if (user1 == null || user2 == null) {
            sendError(player, "对手还没有进入房间");
            return;
        }
        ensureGame();
        if (game == null) {
            sendError(player, "对局尚未开始");
            return;
        }

        JsonNode node;
        try {
            node = objectMapper.readTree(reqJson);
        } catch (Exception e) {
            sendError(player, "非法的请求");
            return;
        }
        if (node == null || node.isMissingNode() || !node.isObject()) {
            sendError(player, "非法的请求");
            return;
        }

        String error = game.applyMove(player, node);
        if (error != null) {
            sendError(player, error);
            return;
        }

        broadcast(game.buildUpdate(player));

        if (game.isFinished()) {
            finish(game.getWinnerId(), game.getEndReason());
        }
    }

    /** 主动认输：判对方获胜 */
    public synchronized void surrender(User player) {
        if (finished || player == null || user1 == null || user2 == null) {
            return;
        }
        if (player.getUserId() != user1.getUserId() && player.getUserId() != user2.getUserId()) {
            return;
        }
        User winner = (player.getUserId() == user1.getUserId()) ? user2 : user1;
        finish(winner.getUserId(), "对手认输");
    }

    /** 掉线/离开：判另一方获胜 */
    public synchronized void playerLeft(User leftUser) {
        if (finished || leftUser == null) {
            return;
        }
        if (user1 == null || user2 == null) {
            finished = true;
            roomManager.remove(this, leftUser.getUserId(), leftUser.getUserId());
            return;
        }
        if (leftUser.getUserId() != user1.getUserId() && leftUser.getUserId() != user2.getUserId()) {
            return;
        }
        User winner = (leftUser.getUserId() == user1.getUserId()) ? user2 : user1;
        finish(winner.getUserId(), "对手已离开，你获胜");
    }

    private void finish(Integer winnerId, String reason) {
        if (finished) {
            return;
        }
        finished = true;

        Map<String, Object> over = new LinkedHashMap<>();
        over.put("message", "gameOver");
        over.put("gameType", gameType);
        over.put("winner", winnerId == null ? 0 : winnerId);
        over.put("reason", reason);
        broadcast(over);

        try {
            if (winnerId == null) {
                userMapper.userDraw(user1.getUserId());
                userMapper.userDraw(user2.getUserId());
            } else {
                int loserId = (winnerId == user1.getUserId()) ? user2.getUserId() : user1.getUserId();
                userMapper.userWin(winnerId);
                userMapper.userLose(loserId);
            }
        } catch (Exception e) {
            System.err.println("[Room.finish] 更新战绩失败: " + e.getMessage());
        }

        System.out.println("[Room] " + gameType + " 房间 " + roomId + " 结束，用时 "
                + (System.currentTimeMillis() - startTime) + "ms，"
                + (winnerId == null ? "平局" : "赢家 userId=" + winnerId) + "（" + reason + "）");

        roomManager.remove(this, user1.getUserId(), user2.getUserId());
    }

    // ================= 消息 =================

    private void broadcast(Map<String, Object> payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            System.err.println("[Room] 序列化消息失败: " + e.getMessage());
            return;
        }
        send(user1, json);
        send(user2, json);
    }

    private void sendError(User user, String reason) {
        GameResponse response = new GameResponse();
        response.setMessage("error");
        response.setReason(reason);
        try {
            send(user, objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            System.err.println("[Room] 发送错误消息失败: " + e.getMessage());
        }
    }

    private void send(User user, String json) {
        if (user == null) {
            return;
        }
        WebSocketSession session = onlineUserManager.getFromGameRoom(user.getUserId());
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                System.err.println("[Room] 发送消息给 " + user.getUsername() + " 失败: " + e.getMessage());
            }
        }
    }

    // ================= getter / setter =================

    public synchronized boolean isFinished() {
        return finished;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = GameFactory.normalize(gameType);
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    /** 房间里的另一方 */
    public User otherUser(User user) {
        if (user == null) {
            return null;
        }
        if (user1 != null && user.getUserId() == user1.getUserId()) {
            return user2;
        }
        if (user2 != null && user.getUserId() == user2.getUserId()) {
            return user1;
        }
        return null;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public User getWhiteUser() {
        return whiteUser;
    }

    public void setWhiteUser(User user) {
        whiteUser = user;
    }
}
