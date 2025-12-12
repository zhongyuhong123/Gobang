package com.yangshengzhou.gobang.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangshengzhou.gobang.entity.User;
import com.yangshengzhou.gobang.entity.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

@Component
public class Matcher {
    private Queue<User> normalQueue = new LinkedList<>();
    private Queue<User> highQueue = new LinkedList<>();
    private Queue<User> veryHighQueue = new LinkedList<>();

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private RoomManager roomManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void add(User user) {
        if(user.getScore()<2000){
            synchronized (normalQueue) {
                normalQueue.add(user);
                normalQueue.notify();
            }
            System.out.println("把玩家 " + user.getUsername() + " 加入到了normalQueue中！");
        }else if(user.getScore()>=2000 && user.getScore()<3000){
            synchronized (highQueue) {
                highQueue.add(user);
                highQueue.notify();
            }
            System.out.println("把玩家 " + user.getUsername() + " 加入到了highQueue中！");
        }else{
            synchronized (veryHighQueue) {
                veryHighQueue.add(user);
                veryHighQueue.notify();
            }
            System.out.println("把玩家 " + user.getUsername() + " 加入到了veryHighQueue中！");
        }
    }

    public void remove(User user) {
        if(user.getScore()<2000){
            synchronized (normalQueue) {
                normalQueue.remove(user);
            }
            System.out.println("把玩家"+user.getUsername()+"移除出normalQueue!");
        }else if(user.getScore()>=2000 && user.getScore()<3000){
            synchronized (highQueue) {
                highQueue.remove(user);
            }
            System.out.println("把玩家"+user.getUsername()+"移除出highQueue!");
        }else{
            synchronized (veryHighQueue) {
                veryHighQueue.remove(user);
            }
            System.out.println("把玩家"+user.getUsername()+"移除出veryHighQueue!");
        }
    }

    public boolean isInMatchQueue(User user) {
        if(user.getScore()<2000){
            synchronized (normalQueue) {
                return normalQueue.contains(user);
            }
        }else if(user.getScore()>=2000 && user.getScore()<3000){
            synchronized (highQueue) {
                return highQueue.contains(user);
            }
        }else{
            synchronized (veryHighQueue) {
                return veryHighQueue.contains(user);
            }
        }
    }

    public User findOpponent(User user, String gameMode) {
        Queue<User> targetQueue;
        if(user.getScore()<2000){
            targetQueue = normalQueue;
        }else if(user.getScore()>=2000 && user.getScore()<3000){
            targetQueue = highQueue;
        }else{
            targetQueue = veryHighQueue;
        }

        synchronized (targetQueue) {
            for (User candidate : targetQueue) {
                if (!candidate.equals(user) && onlineUserManager.getFromGameHall(candidate.getUserId()) != null) {
                    // 找到合适的对手，从队列中移除并返回
                    targetQueue.remove(candidate);
                    return candidate;
                }
            }
        }
        return null;
    }

    //构造方法中创建线程，分别针对三个匹配队列，进行操作
    public Matcher(){
        Thread t1 = new Thread(){
            @Override
            public void run() {
                //扫描 normalQueue
                while(true){
                    handlerMatch(normalQueue);
                }
            }
        };
        t1.start();

        Thread t2 = new Thread(){
            @Override
            public void run() {
                while(true){
                    handlerMatch(highQueue);
                }
            }
        };
        t2.start();

        Thread t3 = new Thread(){
            @Override
            public void run() {
                while(true){
                    handlerMatch(veryHighQueue);
                }
            }
        };
        t3.start();
    }

    private void handlerMatch(Queue<User> matchQueue) {
        synchronized (matchQueue) {
            try{
                while(matchQueue.size()<2){
                    matchQueue.wait();
                }
                User player1 = matchQueue.poll();
                User player2 = matchQueue.poll();
                System.out.println("匹配出两个玩家：" + player1.getUsername() + ", " + player2.getUsername());
                WebSocketSession session1 = onlineUserManager.getFromGameHall(player1.getUserId());
                WebSocketSession session2 = onlineUserManager.getFromGameHall(player2.getUserId());
                if(session1==null){
                    matchQueue.offer(player2);
                    return;
                }
                if(session2==null){
                    matchQueue.offer(player1);
                    return;
                }
                //判断两个队列是否相等
                if(session1==session2){
                    matchQueue.offer(player1);
                    return;
                }

                Room room = new Room();
                roomManager.add(room, player1.getUserId(), player2.getUserId());
                MatchResponse response1 = new MatchResponse();
                response1.setOk(true);
                response1.setMessage("matchSuccess");
                response1.setReason(room.getRoomId()); // 使用reason字段传递房间ID
                String json1 = objectMapper.writeValueAsString(response1);
                session1.sendMessage(new TextMessage(json1));

                MatchResponse response2 = new MatchResponse();
                response2.setOk(true);
                response2.setMessage("matchSuccess");
                response2.setReason(room.getRoomId()); // 使用reason字段传递房间ID
                String json2 = objectMapper.writeValueAsString(response2);
                session2.sendMessage(new TextMessage(json2));

            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}