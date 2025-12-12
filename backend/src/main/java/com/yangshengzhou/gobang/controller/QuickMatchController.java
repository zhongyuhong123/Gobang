package com.yangshengzhou.gobang.controller;

import com.yangshengzhou.gobang.entity.User;
import com.yangshengzhou.gobang.game.Matcher;
import com.yangshengzhou.gobang.game.OnlineUserManager;
import com.yangshengzhou.gobang.game.RoomManager;
import com.yangshengzhou.gobang.entity.Room;
import com.yangshengzhou.gobang.mapper.UserMapper;
import com.yangshengzhou.gobang.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/game/match")
public class QuickMatchController {

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private Matcher matcher;

    @Autowired
    private RoomManager roomManager;

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

            if (matcher.isInMatchQueue(user)) {
                return new ApiResponse(false, "用户已在匹配队列中", null);
            }

            matcher.add(user);

            try {
                Thread.sleep(1000);
                
                User matchedUser = matcher.findOpponent(user, request.getGameMode());
                
                if (matchedUser != null) {
                    String roomId = UUID.randomUUID().toString();
                    Room room = new Room();
                    room.setRoomId(roomId);
                    room.setUser1(user);
                    room.setUser2(matchedUser);
                    room.setWhiteUser(user);
                    
                    roomManager.add(room, user.getUserId(), matchedUser.getUserId());
                    
                    matcher.remove(user);
                    matcher.remove(matchedUser);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("gameId", roomId);
                    result.put("roomId", roomId);
                    result.put("matchedUser", matchedUser.getUsername());
                    
                    return new ApiResponse(true, "匹配成功", result);
                } else {
                    // 没有找到对手，继续等待
                    return new ApiResponse(true, "正在匹配中，请稍后...", null);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return new ApiResponse(false, "匹配过程被中断", null);
            }

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

    // 内部请求类
    public static class QuickMatchRequest {
        private String gameMode;

        public String getGameMode() {
            return gameMode;
        }

        public void setGameMode(String gameMode) {
            this.gameMode = gameMode;
        }
    }

    // 内部响应类
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