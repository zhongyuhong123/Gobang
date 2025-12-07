package org.example.gobang.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.java_gobang.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

//这个类表示“匹配器”，通过这个类负责完成整个匹配功�?
@Component
public class Matcher {
    //创建三个匹配队列
    private Queue<User> normalQueue = new LinkedList<>();
    private Queue<User> highQueue = new LinkedList<>();
    private Queue<User> veryHighQueue = new LinkedList<>();

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private RoomManager roomManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    //操作匹配队列的方�?
    //把玩家放到匹配队列中
    public void add(User user) {
        if(user.getScore()<2000){
            synchronized (normalQueue) {
                normalQueue.add(user);
                normalQueue.notify();
            }
            System.out.println("把玩�? + user.getUsername() + "加入到了normalQueue中！");
        }else if(user.getScore()>=2000 && user.getScore()<3000){
            synchronized (highQueue) {
                highQueue.add(user);
                highQueue.notify();
            }
            System.out.println("把玩�? + user.getUsername() + "加入到了highQueue中！");
        }else{
            synchronized (veryHighQueue) {
                veryHighQueue.add(user);
                veryHighQueue.notify();
            }
            System.out.println("把玩�? + user.getUsername() + "加入到了veryHighQueue中！");
        }
    }

    //当玩家点击停止匹配的时候，就需要把玩家从匹配队列中删除
    public void remove(User user) {
        if(user.getScore()<2000){
            synchronized (normalQueue) {
                normalQueue.remove(user);
            }
            System.out.println("把玩�?+user.getUsername()+"移除�?normalQueue!");
        }else if(user.getScore()>=2000 && user.getScore()<3000){
            synchronized (highQueue) {
                highQueue.remove(user);
            }
            System.out.println("把玩�?+user.getUsername()+"移除�?highQueue!");
        }else{
            synchronized (veryHighQueue) {
                veryHighQueue.remove(user);
            }
            System.out.println("把玩�?+user.getUsername()+"移除�?veryHighQueue!");
        }
    }

    //构造方法中创建线程，分别针对三个匹配队列，进行操作�?
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
                //1.检测队列中元素个数是否达到2
                //当前队列元素不足2，持续等待~~
                while(matchQueue.size()<2){
                    matchQueue.wait();
                }
                //2.尝试从队列中取出两个玩家
                User player1 = matchQueue.poll();
                User player2 = matchQueue.poll();
                System.out.println("匹配出两个玩家：" + player1.getUsername() + ", " + player2.getUsername());
                //3.获取到玩家的 websocket 的会�?
                //     获取到会话的木器是为了告诉玩家，匹配成功
                WebSocketSession session1 = onlineUserManager.getFromGameHall(player1.getUserId());
                WebSocketSession session2 = onlineUserManager.getFromGameHall(player2.getUserId());
                //再对用户的在线状态进行一次判定~
                //如果一个玩家为空，则吧另一玩家放回队列
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

                //4.  把这两个玩家放到一个游戏房间中
                Room room = new Room();
                roomManager.add(room, player1.getUserId(), player2.getUserId());

                //5.给玩家反馈信息：你匹配到对手了~
                //      通过 websocket 返回一�?message �?'matchSuccess' 这样的响�?
                //      两个玩家都需要返�?
                MatchResponse response1 = new MatchResponse();
                response1.setOk(true);
                response1.setMessage("matchSuccess");
                String json1 = objectMapper.writeValueAsString(response1);
                session1.sendMessage(new TextMessage(json1));

                MatchResponse response2 = new MatchResponse();
                response2.setOk(true);
                response2.setMessage("matchSuccess");
                String json2 = objectMapper.writeValueAsString(response2);
                session2.sendMessage(new TextMessage(json2));

            }catch (IOException | InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}









