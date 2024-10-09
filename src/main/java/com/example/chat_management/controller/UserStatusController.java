package com.example.chat_management.controller;

import com.example.chat_management.model.UserStatus;
import com.example.chat_management.service.UserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserStatusController {

    private final UserStatusService userStatusService;

    @Autowired
    public UserStatusController(UserStatusService userStatusService) {
        this.userStatusService = userStatusService;
    }

    // Endpoint to get the user status and last offline timestamp by user number
    @GetMapping("/status/{userNumber}")
    public ResponseEntity<UserStatus> getUserStatus(@PathVariable String userNumber) {
        UserStatus userStatus = userStatusService.getUserStatusByNumber(userNumber);
        if (userStatus != null) {
            return ResponseEntity.ok(userStatus);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
