package com.yangshengzhou.gobang.controller;

import com.yangshengzhou.gobang.entity.User;
import com.yangshengzhou.gobang.game.OnlineUserManager;
import com.yangshengzhou.gobang.game.RoomManager;
import com.yangshengzhou.gobang.entity.Room;
import com.yangshengzhou.gobang.util.JwtTokenUtil;
import com.yangshengzhou.gobang.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/game")
public class GameStatsController {

    @Autowired
    private OnlineUserManager onlineUserManager;

    @Autowired
    private RoomManager roomManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/stats")
    public Object getGameStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            Map<String, Object> gobangStats = new HashMap<>();
            gobangStats.put("online", onlineUserManager.getGameHallUserCount());
            gobangStats.put("activeGames", Math.max(0, onlineUserManager.getGameHallUserCount() / 2));
            
            Map<String, Object> militaryStats = new HashMap<>();
            militaryStats.put("online", Math.max(0, onlineUserManager.getGameHallUserCount() - 5));
            militaryStats.put("activeGames", Math.max(0, (onlineUserManager.getGameHallUserCount() - 5) / 2));
            
            Map<String, Object> chineseChessStats = new HashMap<>();
            chineseChessStats.put("online", Math.max(0, onlineUserManager.getGameHallUserCount() - 10));
            chineseChessStats.put("activeGames", Math.max(0, (onlineUserManager.getGameHallUserCount() - 10) / 2));
            
            stats.put("gobang", gobangStats);
            stats.put("military", militaryStats);
            stats.put("chineseChess", chineseChessStats);
            
            return new ApiResponse(true, "获取游戏统计成功", stats);
        } catch (Exception e) {
            return new ApiResponse(false, "获取游戏统计失败：" + e.getMessage(), null);
        }
    }

    @PostMapping("/room/create")
    public Object createRoom(@RequestBody CreateRoomRequest request, HttpServletRequest httpRequest) {
        try {
            User user = getUserFromToken(httpRequest);
            
            if (user == null) {
                return new ApiResponse(false, "用户未登录", null);
            }

            if (onlineUserManager.getFromGameRoom(user.getUserId()) != null) {
                return new ApiResponse(false, "用户已在游戏中", null);
            }

            String roomId = UUID.randomUUID().toString();
            Room room = new Room();
            room.setRoomId(roomId);
            room.setUser1(user);
            room.setWhiteUser(user);
            
            roomManager.add(room, user.getUserId(), user.getUserId());
            
            Map<String, Object> result = new HashMap<>();
            result.put("roomId", roomId);
            result.put("gameMode", request.getGameMode());
            
            return new ApiResponse(true, "房间创建成功", result);
            
        } catch (Exception e) {
            return new ApiResponse(false, "创建房间失败：" + e.getMessage(), null);
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
    public static class CreateRoomRequest {
        private String gameMode;

        public String getGameMode() {
            return gameMode;
        }

        public void setGameMode(String gameMode) {
            this.gameMode = gameMode;
        }
    }
    
    // 内部类，用于统一API响应格式
    private static class ApiResponse {
        private boolean status;
        private String message;
        private Object data;

        public ApiResponse(boolean status, String message, Object data) {
            this.status = status;
            this.message = message;
            this.data = data;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
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