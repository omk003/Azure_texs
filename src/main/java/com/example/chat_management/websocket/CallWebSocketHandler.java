package com.example.chat_management.websocket;

import com.example.chat_management.service.CallService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
//under work
@Component
public class CallWebSocketHandler implements WebSocketHandler {

    private final CallService callService;

    

    // Keep track of WebSocket sessions by user ID
    private final Map<String, WebSocketSession> sessions = new HashMap<>();

    public CallWebSocketHandler(CallService callService) {
        this.callService = callService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // When a connection is established, add the session to the active sessions
        String userId = getUserIdFromSession(session);
        sessions.put(userId, session);
        
        // Send any pending call offers or messages if the user has any
        callService.sendPendingCalls(userId, session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // Parse the incoming message
        String payload = message.getPayload().toString();
        
        // Example of how to handle different signaling messages
        if (payload.contains("offer")) {
            String receiverId = extractReceiverIdFromMessage(payload);
            sendMessageToReceiver(receiverId, payload);
        } else if (payload.contains("answer")) {
            String senderId = extractSenderIdFromMessage(payload);
            sendMessageToReceiver(senderId, payload);
        } else if (payload.contains("iceCandidate")) {
            String receiverId = extractReceiverIdFromMessage(payload);
            sendMessageToReceiver(receiverId, payload);
        }
    }

    private void sendMessageToReceiver(String receiverId, String message) throws IOException {
        WebSocketSession receiverSession = sessions.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(message));
        } else {
            // If the receiver is not connected, save the message and notify them
            callService.saveMissedCall(receiverId, message);
            callService.sendNotification(receiverId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // Handle errors during transport
        session.close();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // Remove the session when the connection is closed
        String userId = getUserIdFromSession(session);
        sessions.remove(userId);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String getUserIdFromSession(WebSocketSession session) {
        // Extract the user ID from the session (assuming you store it in session attributes)
        return (String) session.getAttributes().get("userId");
    }

    private String extractReceiverIdFromMessage(String message) {
        // Logic to extract receiver ID from message
        return "receiverId";
    }

    private String extractSenderIdFromMessage(String message) {
        // Logic to extract sender ID from message
        return "senderId";
    }
}
