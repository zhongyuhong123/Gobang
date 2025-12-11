package com.yangshengzhou.gobang.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangshengzhou.gobang.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchTimeoutService {

    @Autowired
    private Matcher matcher;

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private ObjectMapper objectMapper;

    // 存储用户开始匹配的时间
    private final Map<Integer, LocalDateTime> userMatchStartTime = new ConcurrentHashMap<>();

    // 最大匹配时间（3分钟）
    private static final int MAX_MATCH_TIME_SECONDS = 180;

    /**
     * 记录用户开始匹配的时间
     */
    public void recordMatchStart(User user) {
        if (user != null) {
            userMatchStartTime.put(user.getUserId(), LocalDateTime.now());
        }
    }

    /**
     * 移除用户匹配记录（当匹配成功或用户取消匹配时）
     */
    public void removeMatchRecord(User user) {
        if (user != null) {
            userMatchStartTime.remove(user.getUserId());
        }
    }

    /**
     * 定时检查匹配超时（每5秒执行一次）
     */
    @Scheduled(fixedDelay = 5000)
    public void checkMatchTimeout() {
        LocalDateTime now = LocalDateTime.now();

        // 遍历所有正在匹配的用户
        for (Map.Entry<Integer, LocalDateTime> entry : userMatchStartTime.entrySet()) {
            int userId = entry.getKey();
            LocalDateTime startTime = entry.getValue();

            // 计算匹配持续时间
            long elapsedSeconds = ChronoUnit.SECONDS.between(startTime, now);

            // 如果超过最大匹配时间
            if (elapsedSeconds >= MAX_MATCH_TIME_SECONDS) {
                handleMatchTimeout(userId);
            }
        }
    }

    /**
     * 处理匹配超时
     */
    private void handleMatchTimeout(int userId) {
        try {
            // 获取用户信息
            WebSocketSession session = onlineUserManager.getFromGameHall(userId);
            if (session == null) {
                userMatchStartTime.remove(userId);
                return;
            }

            // 获取用户对象
            User user = (User) session.getAttributes().get("user");
            if (user == null) {
                userMatchStartTime.remove(userId);
                return;
            }

            // 从匹配队列中移除用户
            matcher.remove(user);

            // 发送匹配失败消息给用户
            MatchResponse response = new MatchResponse();
            response.setOk(false);
            response.setMessage("matchTimeout");
            response.setReason("匹配失败");

            String jsonResponse = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(jsonResponse));

            // 移除匹配记录
            userMatchStartTime.remove(userId);

            System.out.println("用户 " + user.getUsername() + " 匹配超时，已移除匹配队列");

        } catch (IOException e) {
            System.err.println("处理匹配超时失败: " + e.getMessage());
            userMatchStartTime.remove(userId);
        }
    }
}