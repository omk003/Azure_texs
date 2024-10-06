package com.example.chat_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.chat_management.model.ChatMessage;
import com.example.chat_management.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody ChatMessage chatMessage) {
        chatService.sendMessage(chatMessage);
        return ResponseEntity.ok("Message sent successfully.");
    }

    // Additional methods to retrieve messages can be added here
}
