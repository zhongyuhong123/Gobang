package com.yangshengzhou.gobang.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangshengzhou.gobang.entity.Room;
import com.yangshengzhou.gobang.entity.User;
import com.yangshengzhou.gobang.game.games.GameFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 匹配器：按棋种分队列，每种棋一个工作线程，凑够两人就开房。
 */
@Component
public class Matcher {

    private final Map<String, Queue<User>> queues = new ConcurrentHashMap<>();

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private RoomManager roomManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Matcher() {
        for (String type : Arrays.asList(GameFactory.GOBANG, GameFactory.MILITARY, GameFactory.CHINESE_CHESS)) {
            Queue<User> queue = new LinkedList<>();
            queues.put(type, queue);
            startWorker(type, queue);
        }
    }

    private void startWorker(String type, Queue<User> queue) {
        Thread t = new Thread(() -> {
            while (true) {
                handlerMatch(type, queue);
            }
        }, "matcher-" + type);
        t.setDaemon(true);
        t.start();
    }

    public void add(User user, String mode) {
        String type = GameFactory.normalize(mode);
        Queue<User> queue = queues.get(type);
        synchronized (queue) {
            if (queue.contains(user)) {
                return;
            }
            queue.add(user);
            queue.notifyAll();
        }
        System.out.println("把玩家 " + user.getUsername() + " 加入 " + type + " 匹配队列，当前人数 " + queue.size());
    }

    public void remove(User user) {
        for (Queue<User> queue : queues.values()) {
            synchronized (queue) {
                queue.remove(user);
            }
        }
    }

    public boolean isInMatchQueue(User user, String mode) {
        Queue<User> queue = queues.get(GameFactory.normalize(mode));
        synchronized (queue) {
            return queue.contains(user);
        }
    }

    private void handlerMatch(String type, Queue<User> matchQueue) {
        synchronized (matchQueue) {
            try {
                while (matchQueue.size() < 2) {
                    matchQueue.wait();
                }
                User player1 = matchQueue.poll();
                User player2 = matchQueue.poll();
                System.out.println("[" + type + "] 匹配出两个玩家：" + player1.getUsername() + ", " + player2.getUsername());

                WebSocketSession session1 = onlineUserManager.getFromGameHall(player1.getUserId());
                WebSocketSession session2 = onlineUserManager.getFromGameHall(player2.getUserId());
                if (session1 == null) {
                    matchQueue.offer(player2);
                    return;
                }
                if (session2 == null) {
                    matchQueue.offer(player1);
                    return;
                }
                if (session1 == session2) {
                    matchQueue.offer(player1);
                    return;
                }

                Room room = new Room();
                room.setGameType(type);
                room.setUser1(player1);
                room.setUser2(player2);
                room.setWhiteUser(player1);
                room.ensureGame();
                roomManager.add(room, player1.getUserId(), player2.getUserId());

                MatchResponse response1 = new MatchResponse();
                response1.setOk(true);
                response1.setMessage("matchSuccess");
                response1.setReason(room.getRoomId());
                response1.setGameType(type);
                session1.sendMessage(new TextMessage(objectMapper.writeValueAsString(response1)));

                MatchResponse response2 = new MatchResponse();
                response2.setOk(true);
                response2.setMessage("matchSuccess");
                response2.setReason(room.getRoomId());
                response2.setGameType(type);
                session2.sendMessage(new TextMessage(objectMapper.writeValueAsString(response2)));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** 当前各棋种排队人数（统计用） */
    public List<String> queueSnapshot() {
        List<String> lines = new LinkedList<>();
        for (Map.Entry<String, Queue<User>> entry : queues.entrySet()) {
            synchronized (entry.getValue()) {
                lines.add(entry.getKey() + "=" + entry.getValue().size());
            }
        }
        return lines;
    }
}
