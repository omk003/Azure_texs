package com.example.chat_management.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chat_management.model.UserStatus;
import com.example.chat_management.repository.UserStatusRepository;
import com.example.chat_management.service.ChatService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;

import javax.sql.DataSource;
import java.io.IOException;
import java.time.LocalDateTime;
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

    @Autowired
    private UserStatusRepository userStatusRepository;

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
        } else if (payload.startsWith("TYPING:")) {
            handleTypingEvent(payload, session);
        } else if (payload.startsWith("STOPPED_TYPING:")) {
            handleStoppedTypingEvent(payload, session);
        } else {
            handleMessage(payload, session);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String userNumber = getUserNumberBySession(session);
        if (userNumber != null) {
            userSessions.remove(userNumber);
            updateUserStatus(userNumber,"offline");
            super.afterConnectionClosed(session, status);
            System.out.println("User disconnected: " + userNumber);
        }
    }

    private void handleRegistration(@NonNull String message, @NonNull WebSocketSession session) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String sessionID = parts[1];
            //username -> userNumber
            String userNumber = fetchUserNumberFromSessionID(sessionID);
            if (userNumber != null) {
                userSessions.put(userNumber, session);
                updateUserStatus(userNumber,"online");

                if (offlineMessages.containsKey(userNumber)) {
                    Queue<String> messages = offlineMessages.get(userNumber);
                    while (!messages.isEmpty()) {
                        sendMessageToSession(session, messages.poll());
                    }
                    offlineMessages.remove(userNumber);
                }
                System.out.println("User registered: " + userNumber);
            }
        }
    }

    private void handleMessage(@NonNull String message, @NonNull WebSocketSession session) throws IOException {
        String[] parts = message.split(":", 3);
        if (parts.length == 3 && "TO".equals(parts[0])) {
            String recipientNumber = parts[1];
            String messageContent = parts[2];
            String senderNumber = getUserNumberBySession(session);

            WebSocketSession recipientSession = userSessions.get(recipientNumber);
            if (recipientSession != null) {
                sendMessageToSession(recipientSession, senderNumber + ": " + messageContent);
            } else {
                // Check if the recipient exists in the database
                if (recipientExists(recipientNumber)) {
                    // Store the message for offline recipient
                    storeOfflineMessage(recipientNumber, senderNumber + ": " + messageContent);
                } else {
                    // If the recipient doesn't exist, send an error message to the sender
                    sendMessageToSession(session, "ERROR: Recipient " + recipientNumber + " does not exist.");
                }
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

    private String getUserNumberBySession(@NonNull WebSocketSession session) {
        return userSessions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(session))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    private String fetchUserNumberFromSessionID(@NonNull String sessionID) {
        System.out.println("s_id: " + sessionID);
        String query = "SELECT u.contact_number FROM sessions s INNER JOIN users u ON s.user_id = u.id WHERE s.session_id = ?";

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

    public void updateUserStatus(String userNumber, String status) {
        // Use the custom repository method to find by userNumber
        UserStatus userStatus = userStatusRepository.findByUserNumber(userNumber)
                .orElse(new UserStatus()); // Create a new UserStatus if not found
    
        userStatus.setUserNumber(userNumber); // Set the user number
        userStatus.setStatus(status);         // Update the status
    
        // If the user goes offline, set the lastSetOffTimestamp to the current time
        if ("offline".equals(status)) {
            userStatus.setLastSetOffTimestamp(LocalDateTime.now());
        }
    
        // Save the updated or newly created UserStatus object
        userStatusRepository.save(userStatus);
    }

    // Handle typing event
    private void handleTypingEvent(@NonNull String message, @NonNull WebSocketSession session) throws IOException {
        String[] parts = message.split(":", 3); // TYPING:recipientNumber:senderNumber
        if (parts.length == 3) {
            String recipientNumber = parts[1];
            String senderNumber = parts[2];

            WebSocketSession recipientSession = userSessions.get(recipientNumber);
            if (recipientSession != null) {
                sendMessageToSession(recipientSession, "TYPING:" + senderNumber);
            }
        }
    }

    // Handle stopped typing event
    private void handleStoppedTypingEvent(@NonNull String message, @NonNull WebSocketSession session) throws IOException {
        String[] parts = message.split(":", 3); // STOPPED_TYPING:recipientNumber:senderNumber
        if (parts.length == 3) {
            String recipientNumber = parts[1];
            String senderNumber = parts[2];

            WebSocketSession recipientSession = userSessions.get(recipientNumber);
            if (recipientSession != null) {
                sendMessageToSession(recipientSession, "STOPPED_TYPING:" + senderNumber);
            }
        }
    }

    private boolean recipientExists(String recipientNumber) {
        String query = "SELECT COUNT(*) FROM users WHERE contact_number = ?";
    
        try {
            Integer count = this.jdbcTemplate.queryForObject(query, Integer.class, recipientNumber);
            return count != null && count > 0;
        } catch (EmptyResultDataAccessException e) {
            System.err.println("No recipient found for number: " + recipientNumber);
            return false;
        } catch (Exception e) {
            System.err.println("Error checking recipient number in database: " + e.getMessage());
            return false;
        }
    }
    
}