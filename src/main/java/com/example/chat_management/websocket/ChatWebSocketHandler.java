package com.example.chat_management.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chat_management.service.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private static final Map<String, Queue<String>> offlineMessages = new ConcurrentHashMap<>();

    @Autowired
    private ChatService chatService;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ChatWebSocketHandler(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        System.out.println("A new client connected: " + session.getId());
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String payload = message.getPayload();

        if (payload.startsWith("REGISTER:")) {
            handleRegistration(payload, session);
        } else {
            handleMessage(payload, session);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String username = getUsernameBySession(session);
        if (username != null) {
            userSessions.remove(username);
            super.afterConnectionClosed(session, status);
            System.out.println("User disconnected: " + username);
        }
    }

    private void handleRegistration(@NonNull String message, @NonNull WebSocketSession session) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String sessionID = parts[1];
            String username = fetchUsernameFromSessionID(sessionID);
            if (username != null) {
                userSessions.put(username, session);

                if (offlineMessages.containsKey(username)) {
                    Queue<String> messages = offlineMessages.get(username);
                    while (!messages.isEmpty()) {
                        sendMessageToSession(session, messages.poll());
                    }
                    offlineMessages.remove(username);
                }
                System.out.println("User registered: " + username);
            }
        }
    }

    private void handleMessage(@NonNull String message, @NonNull WebSocketSession session) throws IOException {
        String[] parts = message.split(":", 3);
        if (parts.length == 3 && "TO".equals(parts[0])) {
            String recipientName = parts[1];
            String messageContent = parts[2];
            String senderName = getUsernameBySession(session);

            WebSocketSession recipientSession = userSessions.get(recipientName);
            if (recipientSession != null) {
                sendMessageToSession(recipientSession, senderName + ": " + messageContent);
            } else {
                storeOfflineMessage(recipientName, senderName + ": " + messageContent);
            }
        }
    }

    private void sendMessageToSession(@NonNull WebSocketSession session, @NonNull String message) {
        try {
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            System.err.println("Error sending message to session " + session.getId() + ": " + e.getMessage());
        }
    }

    private void storeOfflineMessage(@NonNull String recipientName, @NonNull String message) {
        offlineMessages.computeIfAbsent(recipientName, k -> new ConcurrentLinkedQueue<>()).add(message);
        System.out.println("Stored offline message for " + recipientName);
    }

    private String getUsernameBySession(@NonNull WebSocketSession session) {
        return userSessions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(session))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    private String fetchUsernameFromSessionID(@NonNull String sessionID) {
        System.out.println("s_id: " + sessionID);
        String query = "SELECT u.username FROM sessions s INNER JOIN users u ON s.user_id = u.id WHERE s.session_id = ?";

        try {
            return this.jdbcTemplate.queryForObject(query, String.class, sessionID);
        } catch (EmptyResultDataAccessException e) {
            System.err.println("No user found for session ID: " + sessionID);
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching username from session ID: " + e.getMessage());
            return null;
        }
    }
}