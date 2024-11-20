package com.example.chat_management.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.lang.NonNull;

import com.example.chat_management.websocket.ChatWebSocketHandler;
import com.example.chat_management.websocket.CallWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final CallWebSocketHandler callWebSocketHandler;

    
    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler, CallWebSocketHandler callWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.callWebSocketHandler = callWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        // Existing chat WebSocket handler
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOrigins("*");
        
        // New call signaling WebSocket handler
        registry.addHandler(callWebSocketHandler, "/ws/call")
                .setAllowedOrigins("*");
    }
}
