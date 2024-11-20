package com.example.chat_management.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.chat_management.model.User;
import com.example.chat_management.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/users/register")
    public ResponseEntity<String> registerUser(@RequestBody User user, HttpSession session) {
        System.out.println("Received User: " + user);

        if (userService.isUsernameTaken(user.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken.");
        }
        User registeredUser = userService.registerUser(user);

        // Store user info in session
        session.setAttribute("username", registeredUser.getUsername());

        // Save session ID to the database
        userService.saveSession(session.getId(), registeredUser.getId());

        return ResponseEntity.ok("Registration successful.");
    }

    @PostMapping("/users/login")
    public ResponseEntity<String> login(@RequestBody User user, HttpSession session) {
        User validatedUser = userService.validateUserCredentials(user.getUsername(), user.getPassword());
        if (validatedUser == null) {
            return ResponseEntity.status(401).body("Invalid username or password.");
        }

        // Store user info in session
        session.setAttribute("username", validatedUser.getUsername());

        // Save session ID to the database
        userService.saveSession(session.getId(), validatedUser.getId());

        return ResponseEntity.ok("Login successful.");
    }

    @GetMapping("/users/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return ResponseEntity.ok("Logout successful.");
    }
}
