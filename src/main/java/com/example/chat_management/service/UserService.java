package com.example.chat_management.service;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.chat_management.model.User;
import com.example.chat_management.repository.UserRepository;

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

    public boolean isContactTaken(String contact) {
        return userRepository.existsByContactNumber(contact);
    }

    public User registerUser(User user) {
        return userRepository.save(user);
    }

    public User validateUserCredentials(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        //remove this code
        return null; // or throw an exception
    }

    // Method to find a user by contact number
    public User findByContactNumber(String contactNumber) {
        Optional<User> user = userRepository.findByContactNumber(contactNumber);

        if (user.isPresent()) {
            return user.get();
        } else {
            throw new RuntimeException("User with contact number " + contactNumber + " not found.");
        }
    }

     // Method to save or update a user
     public void save(User user) {
        userRepository.save(user);
    }
    

    public void saveSession(String uid, String phonenumber) {
        String query = "INSERT INTO sessions (uid, phonenumber) VALUES (?, ?) ON DUPLICATE KEY UPDATE uid = VALUES(uid);";
        jdbcTemplate.update(query, uid, phonenumber);
    }
    
}
