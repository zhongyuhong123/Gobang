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
            // 从JWT token中获取用户信息
            User user = getUserFromToken(httpRequest);
            
            // 检查用户是否登录
            if (user == null) {
                return new ApiResponse(false, "用户未登录", null);
            }

            // 检查用户是否已经在游戏中
            if (onlineUserManager.getFromGameRoom(user.getUserId()) != null) {
                return new ApiResponse(false, "用户已在游戏中", null);
            }

            // 检查用户是否已经在匹配队列中
            if (matcher.isInMatchQueue(user)) {
                return new ApiResponse(false, "用户已在匹配队列中", null);
            }

            // 将用户添加到匹配队列
            matcher.add(user);

            // 模拟快速匹配过程（实际应用中应该有更复杂的匹配逻辑）
            // 这里简化处理，直接创建一个房间
            try {
                Thread.sleep(1000); // 模拟匹配时间
                
                // 检查是否有其他等待匹配的用户
                User matchedUser = matcher.findOpponent(user, request.getGameMode());
                
                if (matchedUser != null) {
                    // 创建房间
                    String roomId = UUID.randomUUID().toString();
                    Room room = new Room();
                    room.setRoomId(roomId);
                    room.setUser1(user);
                    room.setUser2(matchedUser);
                    room.setWhiteUser(user); // 随机设置先手
                    
                    roomManager.add(room, user.getUserId(), matchedUser.getUserId());
                    
                    // 从匹配队列中移除
                    matcher.remove(user);
                    matcher.remove(matchedUser);
                    
                    // 返回房间信息
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

    /**
     * 从JWT token中提取用户信息
     */
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