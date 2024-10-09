package com.example.chat_management.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class UserStatus {

    @Id
    private String userNumber;
    private String status;
    private LocalDateTime lastSetOffTimestamp;

    // Default constructor
    public UserStatus() {}

    // Constructor with parameters
    public UserStatus(String userNumber, String status, LocalDateTime lastSetOffTimestamp) {
        this.userNumber = userNumber;
        this.status = status;
        this.lastSetOffTimestamp = lastSetOffTimestamp;
    }

    // Getters and Setters

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastSetOffTimestamp() {
        return lastSetOffTimestamp;
    }

    public void setLastSetOffTimestamp(LocalDateTime lastSetOffTimestamp) {
        this.lastSetOffTimestamp = lastSetOffTimestamp;
    }
}
