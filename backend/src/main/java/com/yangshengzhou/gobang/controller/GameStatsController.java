package com.yangshengzhou.gobang.controller;

import com.yangshengzhou.gobang.game.OnlineUserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/game")
public class GameStatsController {

    @Autowired
    private OnlineUserManager onlineUserManager;

    @GetMapping("/stats")
    public Object getGameStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // 模拟游戏统计数据
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