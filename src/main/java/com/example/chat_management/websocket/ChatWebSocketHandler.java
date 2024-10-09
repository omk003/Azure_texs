package com.example.chat_management.websocket;

import com.example.chat_management.model.Message;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chat_management.model.UserStatus;
import com.example.chat_management.repository.UserStatusRepository;
import com.example.chat_management.service.ChatService;
import com.example.chat_management.service.MessageService;

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
import java.util.List;


@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private static final Map<String, Queue<String>> offlineMessages = new ConcurrentHashMap<>();

    @Autowired
    private ChatService chatService;

    @Autowired
    private MessageService messageService;

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
            // Fetch userNumber from the session ID
            String userNumber = fetchUserNumberFromSessionID(sessionID);
            if (userNumber != null) {
                // Add the user session
                userSessions.put(userNumber, session);
                // Update the user status to online
                updateUserStatus(userNumber, "online");
    
                // Check if there are offline messages for this user
                if (offlineMessages.containsKey(userNumber)) {
                    Queue<String> messages = offlineMessages.get(userNumber);
    
                    // Process each offline message
                    while (!messages.isEmpty()) {
                        String offlineMessage = messages.poll();
    
                        // Extract sender number from the offline message format (assuming it is "senderNumber: messageContent")
                        String[] messageParts = offlineMessage.split(": ", 2);
                        if (messageParts.length == 2) {
                            String senderNumber = messageParts[0]; // Get the sender's number
                            String messageContent = messageParts[1]; // Get the actual message
    
                            // Send the message to the online recipient (this user)
                            sendMessageToSession(session, offlineMessage);
    
                            // Notify the sender (if they are online) with the "DELIVERED" status
                            WebSocketSession senderSession = userSessions.get(senderNumber);
                            if (senderSession != null) {
                                sendMessageToSession(senderSession, "DELIVERED:" + userNumber + ":" + messageContent);
                            }
                        }
                    }
                    // Remove the messages from offline storage after processing
                    offlineMessages.remove(userNumber);
                }
                System.out.println("User registered: " + userNumber);
            }
        }
    }
    

    private void handleMessage(@NonNull String message, @NonNull WebSocketSession session) throws IOException {
        // Splitting message in the form of TO:recipientNumber:encryptedMessage
        String[] parts = message.split(":", 3);
        
        if (parts.length == 3 && "TO".equals(parts[0])) {
            String recipientNumber = parts[1];        // Recipient's phone number
            String messageContent = parts[2];         // Encrypted message content
            String senderNumber = getUserNumberBySession(session);  // Get sender's number from session
    
            // Check if the recipient is online by looking up the WebSocketSession
            WebSocketSession recipientSession = userSessions.get(recipientNumber);
            
            if (recipientSession != null) {
                // If the recipient is online, send the message directly
                sendMessageToSession(recipientSession, senderNumber + ": " + messageContent);
    
                // Send a 'DELIVERED' status back to the sender, showing the recipient number and message
                sendMessageToSession(session, "DELIVERED:" + recipientNumber + ":" + messageContent);
    
            } else {
                // If the recipient is offline, check if the recipient exists
                if (recipientExists(recipientNumber)) {
                    // Store the offline message in a queue to be delivered later
                    messageService.saveMessage(senderNumber, recipientNumber, messageContent);
    
                    // Notify the sender that the message is sent but not delivered
                    sendMessageToSession(session, "SENT:" + recipientNumber + ":" + messageContent);
                } else {
                    // Notify the sender if the recipient does not exist
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

    public void sendOfflineMessages(String receiverNumber, WebSocketSession session) throws IOException {
        // Retrieve messages from the database for the user
        List<Message> messages = messageService.getMessagesForUser(receiverNumber);

        // Send each message to the recipient
        for (Message msg : messages) {
            sendMessageToSession(session, msg.getSenderNumber() + ": " + msg.getMessageContent());
        }
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