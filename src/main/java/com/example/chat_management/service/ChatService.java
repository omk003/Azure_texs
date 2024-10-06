package com.example.chat_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chat_management.model.ChatMessage;
import com.example.chat_management.repository.ChatMessageRepository;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public void sendMessage(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }

    public List<ChatMessage> getMessages(String fromUser, String toUser) {
        return chatMessageRepository.findByFromUserAndToUser(fromUser, toUser);
    }
}
