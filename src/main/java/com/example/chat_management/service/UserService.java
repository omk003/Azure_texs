package com.example.chat_management.service;


import org.springframework.stereotype.Service;

import com.example.chat_management.model.User;
import com.example.chat_management.repository.UserRepository;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
     @Autowired
    private JdbcTemplate jdbcTemplate;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    public User registerUser(User user) {
        String hashedPassword = hashPassword(user.getPassword());
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }

    public User validateUserCredentials(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user.get();
        }
        return null; // or throw an exception
    }

    private String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public void saveSession(String sessionId, Long userId) {
        String query = "INSERT INTO sessions (session_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(query, sessionId, userId);
    }
    
}
