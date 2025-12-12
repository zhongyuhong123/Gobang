package com.yangshengzhou.gobang.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangshengzhou.gobang.game.MatchRequest;
import com.yangshengzhou.gobang.game.MatchResponse;
import com.yangshengzhou.gobang.game.Matcher;
import com.yangshengzhou.gobang.game.OnlineUserManager;
import com.yangshengzhou.gobang.game.MatchTimeoutService;
import com.yangshengzhou.gobang.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class MatchController extends TextWebSocketHandler {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private Matcher matcher;

    @Autowired
    private MatchTimeoutService matchTimeoutService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        try{
            User user = (User) session.getAttributes().get("user");

            if (user == null) {
                MatchResponse response = new MatchResponse();
                response.setOk(false);
                response.setReason("您尚未登录！不能进行匹配功能！");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
                return;
            }
            if(onlineUserManager.getFromGameHall(user.getUserId()) != null
                    || onlineUserManager.getFromGameRoom(user.getUserId()) != null){
                MatchResponse matchResponse = new MatchResponse();
                matchResponse.setOk(true);
                matchResponse.setReason("禁止多开游戏页面！");
                matchResponse.setMessage("repeatConnection");
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(matchResponse)));
                return;
            }

            onlineUserManager.enterGameHall(user.getUserId(), session);
            System.out.println("玩家"+user.getUsername()+"进入游戏大厅！");
        } catch (NullPointerException e){
            e.printStackTrace();
            MatchResponse response = new MatchResponse();
            response.setOk(false);
            response.setReason("您尚未登录！不能进行匹配功能！");
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        User user = (User) session.getAttributes().get("user");
        String payload = message.getPayload();
        MatchRequest request = objectMapper.readValue(payload, MatchRequest.class);
        MatchResponse response = new MatchResponse();
        if(request.getMessage().equals("startMatch")) {
            matcher.add(user);
            matchTimeoutService.recordMatchStart(user);
            response.setOk(true);
            response.setMessage("startMatch");
        }else if(request.getMessage().equals("stopMatch")){
            matcher.remove(user);
            matchTimeoutService.removeMatchRecord(user);
            response.setOk(true);
            response.setMessage("stopMatch");
        }else{
            response.setOk(false);
            response.setReason("非法的匹配请求");
        }
        String jsonString = objectMapper.writeValueAsString(response);
        session.sendMessage(new TextMessage(jsonString));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        try{
            User user = (User) session.getAttributes().get("user");
            onlineUserManager.exitGameHall(user.getUserId());
            WebSocketSession tmpSession = onlineUserManager.getFromGameHall(user.getUserId());
            if(tmpSession != null){
                onlineUserManager.exitGameHall(user.getUserId());
            }
            matcher.remove(user);
            matchTimeoutService.removeMatchRecord(user);
        } catch (NullPointerException e){
            System.out.println("[MatchController.handleTransportError] 当前用户未登录！");
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try{
            User user = (User) session.getAttributes().get("user");
            WebSocketSession tmpSession = onlineUserManager.getFromGameHall(user.getUserId());
            if(tmpSession != null){
                onlineUserManager.exitGameHall(user.getUserId());
            }
            matcher.remove(user);
        } catch (NullPointerException e){
            System.out.println("[MatchController.afterConnectionClosed] 当前用户未登录！");
        }
    }
}