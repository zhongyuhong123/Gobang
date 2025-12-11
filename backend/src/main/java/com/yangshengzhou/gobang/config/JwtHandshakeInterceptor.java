package com.yangshengzhou.gobang.config;

import com.yangshengzhou.gobang.entity.User;
import com.yangshengzhou.gobang.mapper.UserMapper;
import com.yangshengzhou.gobang.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        
        String token = extractToken(request);
        if (token != null) {
            try {
                if (jwtTokenUtil.validateAccessToken(token)) {
                    String username = jwtTokenUtil.getUsernameFromToken(token);
                    User user = userMapper.selectByName(username);
                    if (user != null) {
                        attributes.put("user", user);
                        return true;
                    }
                }
            } catch (Exception e) {
                System.err.println("JWT token验证失败: " + e.getMessage());
            }
        }
        
        // 如果无法验证用户，仍然允许握手，但用户对象将为null
        return true;
    }

    private String extractToken(ServerHttpRequest request) {
        // 从Authorization头中提取token
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 从查询参数中提取token（用于WebSocket连接）
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");
            if (token != null && !token.trim().isEmpty()) {
                return token;
            }
        }
        
        return null;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                             WebSocketHandler wsHandler, Exception exception) {
        // 握手完成后不需要做额外处理
    }
}
