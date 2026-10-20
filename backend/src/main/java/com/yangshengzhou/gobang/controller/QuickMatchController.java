package com.yangshengzhou.gobang.controller;

import com.yangshengzhou.gobang.entity.User;
import com.yangshengzhou.gobang.game.Matcher;
import com.yangshengzhou.gobang.game.OnlineUserManager;
import com.yangshengzhou.gobang.game.games.GameFactory;
import com.yangshengzhou.gobang.mapper.UserMapper;
import com.yangshengzhou.gobang.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 通过 REST 主动加入匹配队列。
 *
 * 匹配成功仍然通过 /findMatch 这条 WebSocket 通知，所以要求用户已经连上匹配 socket。
 */
@RestController
@RequestMapping("/game/match")
public class QuickMatchController {

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private Matcher matcher;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/quick")
    public Object quickMatch(@RequestBody QuickMatchRequest request, HttpServletRequest httpRequest) {
        try {
            User user = getUserFromToken(httpRequest);
            if (user == null) {
                return new ApiResponse(false, "用户未登录", null);
            }
            if (onlineUserManager.getFromGameRoom(user.getUserId()) != null) {
                return new ApiResponse(false, "用户已在游戏中", null);
            }
            if (onlineUserManager.getFromGameHall(user.getUserId()) == null) {
                return new ApiResponse(false, "请先连接匹配服务（WebSocket）", null);
            }

            String mode = GameFactory.normalize(request.getGameMode());
            if (matcher.isInMatchQueue(user, mode)) {
                return new ApiResponse(false, "用户已在匹配队列中", null);
            }

            onlineUserManager.setHallMode(user.getUserId(), mode);
            matcher.add(user, mode);

            Map<String, Object> data = new HashMap<>();
            data.put("gameMode", mode);
            return new ApiResponse(true, "已加入匹配队列，匹配成功后会通过 WebSocket 通知", data);
        } catch (Exception e) {
            return new ApiResponse(false, "快速匹配失败：" + e.getMessage(), null);
        }
    }

    private User getUserFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtTokenUtil.validateAccessToken(token)) {
                    String username = jwtTokenUtil.getUsernameFromToken(token);
                    return userMapper.selectByName(username);
                }
            } catch (Exception e) {
                System.err.println("JWT token验证失败: " + e.getMessage());
            }
        }
        return null;
    }

    public static class QuickMatchRequest {
        private String gameMode;

        public String getGameMode() {
            return gameMode;
        }

        public void setGameMode(String gameMode) {
            this.gameMode = gameMode;
        }
    }

    private static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
