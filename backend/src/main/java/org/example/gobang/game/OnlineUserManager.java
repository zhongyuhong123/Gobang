package org.example.gobang.game;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserManager {
    //这个哈希表就用来表示当前用户在游戏大厅的在线状态�?
    //这里的hash表存在多个用户和服务器建立连�?断开连接，此时服务器就是并发�?
    private ConcurrentHashMap<Integer, WebSocketSession> gameHall = new ConcurrentHashMap<>();
    //这个哈希表就用来表示用户在游戏房间的在线状态�?
    private ConcurrentHashMap<Integer, WebSocketSession> gameRoom = new ConcurrentHashMap<>();

    public void enterGameHall(int userId, WebSocketSession webSocketSession) {
        gameHall.put(userId, webSocketSession);
    }

    public void exitGameHall(int userId) {
        gameHall.remove(userId);
    }

    public WebSocketSession getFromGameHall(int userId) {
        return gameHall.get(userId);
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
}
