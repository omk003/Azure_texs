package com.example.chat_management.controller;

import java.util.Map;

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

        boolean check  = userService.isContactTaken(user.getContactNumber());
        if(check){
            User checkUser = userService.findByContactNumber(user.getContactNumber());
            checkUser.setUsername(user.getUsername());
            userService.save(checkUser);

        }else{
            userService.registerUser(user);
        }

        

        // Store user info in session
        //session.setAttribute("username", registeredUser.getUsername());

        // Save session ID to the database
        // deprecated way, now we are only using login to store sessionID
        //userService.saveSession(session.getId(), registeredUser.getId());

        return ResponseEntity.ok("Registration successful.");
    }

    @PostMapping("/users/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> payload, HttpSession session) {
        String phoneNumber = payload.get("phoneNumber");
        String uid = payload.get("uid");

        // User validatedUser = userService.validateUserCredentials(user.getUsername(), user.getPassword());
        // if (validatedUser == null) {
        //     return ResponseEntity.status(401).body("Invalid username or password.");
        // }

        // Store user info in session
        // deprecated authentication system
        //session.setAttribute("username", validatedUser.getUsername());

        // Save session ID to the database
        userService.saveSession(uid, phoneNumber);

        return ResponseEntity.ok("User registered and session created successfully.");
    }

    @GetMapping("/users/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate(); // Invalidate the session
        return ResponseEntity.ok("Logout successful.");
    }
}
