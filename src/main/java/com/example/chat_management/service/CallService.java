package com.example.chat_management.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.springframework.web.socket.TextMessage;

//under work
@Service
public class CallService {

    // A mock database to hold missed calls
    private final Map<String, List<String>> missedCalls = new HashMap<>();

    // Simulate saving missed calls for offline users
    public void saveMissedCall(String userId, String message) {
        missedCalls.computeIfAbsent(userId, k -> new ArrayList<>()).add(message);
    }

    // Simulate sending notifications to offline users
    public void sendNotification(String userId) {
        // Notify the user that they have missed a call
        System.out.println("Sent a notification to user: " + userId);
    }

    // Send pending calls when the user reconnects
    public void sendPendingCalls(String userId, WebSocketSession session) throws IOException {
        List<String> pendingCalls = missedCalls.get(userId);
        if (pendingCalls != null) {
            for (String call : pendingCalls) {
                session.sendMessage(new TextMessage(call));
            }
            missedCalls.remove(userId);  // Clear missed calls after sending
        }
    }
}
