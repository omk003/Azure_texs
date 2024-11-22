package com.example.chat_management.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/users")
public class UserSearchController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;  // For JSON parsing and formatting

    @PostMapping("/search")
    public ResponseEntity<String> searchUser(@RequestBody String requestData, HttpServletRequest request) {
        String sessionId = request.getHeader("sessionID");
        
        // Validate session ID
        if (!isSessionValid(sessionId)) {
            return ResponseEntity.status(401).body(createErrorResponse("Invalid or expired session."));
        }

        try {
            // Parse JSON request data using Jackson
            JsonNode jsonRequest = objectMapper.readTree(requestData);
            String query = jsonRequest.get("query").asText();

            // Search for users by username
            List<Map<String, Object>> searchResults = searchUsername(query);

            // Convert the result into a JSON string and return the response
            String jsonResponse = objectMapper.writeValueAsString(searchResults);
            return ResponseEntity.ok(jsonResponse);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Internal server error"));
        }
    }

    // Method to check if the session is valid
    private boolean isSessionValid(String sessionId) {
        String query = "SELECT COUNT(*) FROM sessions WHERE uid = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, sessionId);
        return count != null && count > 0;
    }

    // Method to search users by username
    private List<Map<String, Object>> searchUsername(String query) {
        String sql = "SELECT username, contact_number FROM users WHERE contact_number LIKE ?";
        return jdbcTemplate.queryForList(sql, "%" + query + "%");
    }

    // Method to create error response in JSON format
    private String createErrorResponse(String message) {
        return "{\"error\": \"" + message + "\"}";
    }
}
