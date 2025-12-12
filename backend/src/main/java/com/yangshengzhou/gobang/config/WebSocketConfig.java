package com.yangshengzhou.gobang.config;

import com.yangshengzhou.gobang.controller.GameController;
import com.yangshengzhou.gobang.controller.MatchController;
import com.yangshengzhou.gobang.controller.TestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{

    @Autowired
    private TestController testController;

    @Autowired
    private MatchController matchController;

    @Autowired
    private GameController gameController;

    @Autowired
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(testController,"/test");
        registry.addHandler(matchController,"/findMatch")
                .addInterceptors(jwtHandshakeInterceptor);
        registry.addHandler(gameController,"/game")
                .addInterceptors(jwtHandshakeInterceptor);
    }
}