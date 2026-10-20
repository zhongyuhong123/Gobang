package com.yangshengzhou.gobang.game;

import com.yangshengzhou.gobang.game.games.GameFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserManager {
    private final ConcurrentHashMap<Integer, WebSocketSession> gameHall = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, WebSocketSession> gameRoom = new ConcurrentHashMap<>();
    /** 每个在大厅的玩家选择的棋种，用于按棋种统计在线人数 */
    private final ConcurrentHashMap<Integer, String> hallMode = new ConcurrentHashMap<>();

    public void enterGameHall(int userId, WebSocketSession webSocketSession) {
        gameHall.put(userId, webSocketSession);
    }

    public void exitGameHall(int userId) {
        gameHall.remove(userId);
        hallMode.remove(userId);
    }

    public WebSocketSession getFromGameHall(int userId) {
        return gameHall.get(userId);
    }

    public void setHallMode(int userId, String mode) {
        hallMode.put(userId, GameFactory.normalize(mode));
    }

    public String getHallMode(int userId) {
        return hallMode.getOrDefault(userId, GameFactory.GOBANG);
    }

    /** 某个棋种当前在大厅等待的人数 */
    public int countHallUsers(String mode) {
        String type = GameFactory.normalize(mode);
        int count = 0;
        for (Map.Entry<Integer, WebSocketSession> entry : gameHall.entrySet()) {
            String m = hallMode.get(entry.getKey());
            if (m == null) {
                m = GameFactory.GOBANG;
            }
            if (type.equals(m)) {
                count++;
            }
        }
        return count;
    }

    public void enterGameRoom(int userId, WebSocketSession webSocketSession) {
        gameRoom.put(userId, webSocketSession);
    }

    public void exitGameRoom(int userId) {
        gameRoom.remove(userId);
    }

    public WebSocketSession getFromGameRoom(int userId) {
        return gameRoom.get(userId);
    }

    public int getGameHallUserCount() {
        return gameHall.size();
    }

    public int getGameRoomUserCount() {
        return gameRoom.size();
    }
}
