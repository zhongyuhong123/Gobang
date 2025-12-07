package org.example.gobang.api;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.java_gobang.game.MatchRequest;
import org.example.java_gobang.game.MatchResponse;
import org.example.java_gobang.game.Matcher;
import org.example.java_gobang.game.OnlineUserManager;
import org.example.java_gobang.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

//通过这个类来处理匹配功能中的 websocket 请求
@Component
public class MatchAPI extends TextWebSocketHandler {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private Matcher matcher;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //玩家上线，加入onlineUserManager�?

        //1.先获取到当前用户的身份信息（谁在游戏大厅中，建立的连接）
        //此处的代码，之所以能够getAttributes, 全靠了在注册websocket的时�?
        //加上的addInterceptors(new HttpSessionHandshakeInterceptor())�?
        //这个逻辑就把 HttpSession 中的 Attributes 都给�?WebSocketSession 中了
        //在http登录逻辑中， 往 HttpSession 中存了User数据：session.setAttribute("user", user);
        //此时就可以在 webSocketSession 中把之前 HttpSession 里存的User对象给拿到了�?
        //主意，此处拿到的user，是有可能为空的！！
        //存在可能之前用户压根就没有通过HTTP来进行登录，直接就通过/game_hall.html这个url来访问游戏大厅页�?
        try{
            User user = (User) session.getAttributes().get("user");

            if (user == null) {
                MatchResponse response = new MatchResponse();
                response.setOk(false);
                response.setReason("您尚未登录！不能进行匹配功能�?);
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                return;
            }
            //2.先判定当前用户是否已经是在线状态了，如果是，则直接跳出
            if(onlineUserManager.getFromGameHall(user.getUserId()) != null
                    || onlineUserManager.getFromGameRoom(user.getUserId()) != null){
                //当前用户已经在线了！�?
                MatchResponse matchResponse = new MatchResponse();
                matchResponse.setOk(true);
                matchResponse.setReason("禁止多开游戏页面�?);
                matchResponse.setMessage("repeatConnection");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(matchResponse)));
               //这里 close 太激进了。直接触发客户端 websocket �?onclose 逻辑�?
                //session.close();
                return;
            }

            //3.拿到了身份信息之后，就可以把玩家的设置成在线状态了
            onlineUserManager.enterGameHall(user.getUserId(), session);
            System.out.println("玩家"+user.getUsername()+"进入游戏大厅�?);
        } catch (NullPointerException e){
            e.printStackTrace();
            //出现空指针异常，说明当前用户的身份信息是空，用户未登录�?
            //把当前用户尚未登录这个信息返回回去~
            MatchResponse response = new MatchResponse();
            response.setOk(false);
            response.setReason("您尚未登录！不能进行匹配功能�?);
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //实现处理开始匹配请求和停止匹配请求
        User user = (User) session.getAttributes().get("user");
        //获取到客户端给服务器发送的数据
        String payload = message.getPayload();
        //当前这个数据载荷是一个JSON格式的字符串，就需要把他转成Java对象，MatchRequest
        MatchRequest request = objectMapper.readValue(payload, MatchRequest.class);
        MatchResponse response = new MatchResponse();
        if(request.getMessage().equals("startMatch")) {
            //进入匹配队列
            matcher.add(user);
            //把玩家信息放入匹配队列之后，就可以返回一个响应客户端�?
            response.setOk(true);
            response.setMessage("startMatch");
        }else if(request.getMessage().equals("stopMatch")){
            //退出匹配队�?
            matcher.remove(user);
            //移除之后，就可以返回一个响应给客户端了�?
            response.setOk(true);
            response.setMessage("stopMatch");
        }else{
            //非法情况
            response.setOk(false);
            response.setReason("非法的匹配请�?);
        }
        //服务器需要将响应返回客户端通过   session.sendMessage()
        String jsonString = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonString));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        try{
            //玩家下线，从OnlineUserManager中删�?
            User user = (User) session.getAttributes().get("user");
            onlineUserManager.exitGameHall(user.getUserId());
            WebSocketSession tmpSession = onlineUserManager.getFromGameHall(user.getUserId());
            if(tmpSession != null){
                onlineUserManager.exitGameHall(user.getUserId());
            }
            //玩家在匹配中，而websocket连接断开，则移除队列
            matcher.remove(user);
        } catch (NullPointerException e){
//            e.printStackTrace();
            System.out.println("[MatchAPI.handleTransportError] 当前用户未登录！");

            //已经关闭 websocket 连接不应该继续尝试发送消�?
//            MatchResponse response = new MatchResponse();
//            response.setOk(false);
//            response.setReason("您尚未登录！不能进行匹配功能�?);
//            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try{
            //玩家下线，从OnlineUserManager中删�?
            User user = (User) session.getAttributes().get("user");
            WebSocketSession tmpSession = onlineUserManager.getFromGameHall(user.getUserId());
            if(tmpSession != null){
                onlineUserManager.exitGameHall(user.getUserId());
            }
            //玩家在匹配中，而websocket连接断开，则移除队列
            matcher.remove(user);
        } catch (NullPointerException e){
//            e.printStackTrace();
            System.out.println("[MatchAPI.afterConnectionClosed] 当前用户未登录！");

//            MatchResponse response = new MatchResponse();
//            response.setOk(false);
//            response.setReason("您尚未登录！不能进行匹配功能�?);
//            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }
    }
}












