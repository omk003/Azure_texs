package com.example.chat_management.service;

import com.example.chat_management.model.UserStatus;
import com.example.chat_management.repository.UserStatusRepository;
//import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserStatusService {

    private final UserStatusRepository userStatusRepository;

    public UserStatusService(UserStatusRepository userStatusRepository) {
        this.userStatusRepository = userStatusRepository;
    }

    // Method to get user status by user number
    public UserStatus getUserStatusByNumber(String userNumber) {
        Optional<UserStatus> userStatusOptional = userStatusRepository.findByUserNumber(userNumber);
        
        // If the user status is present, return it; otherwise, return a default offline status
        return userStatusOptional.orElseGet(() -> new UserStatus(userNumber, "offline", LocalDateTime.now()));
    }
}
