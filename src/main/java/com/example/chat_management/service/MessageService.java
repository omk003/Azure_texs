package com.example.chat_management.service;

import com.example.chat_management.model.Message;
import com.example.chat_management.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public void saveMessage(String senderNumber, String receiverNumber, String messageContent) {
        Message message = new Message(senderNumber, receiverNumber, messageContent);
        messageRepository.save(message);
    }

    public List<Message> getMessagesForUser(String receiverNumber) {
        return messageRepository.findByReceiverNumber(receiverNumber);
    }
}
