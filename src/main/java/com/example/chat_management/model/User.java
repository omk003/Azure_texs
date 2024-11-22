package com.example.chat_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // private String firstName;
    // private String lastName;
    //@Column(unique = true, nullable = false)//changed
    private String username;
    
    @Column(unique = true, nullable = false)
    private String contactNumber;

    // Constructors
    public User() {}

    public User(String username, String contactNumber) {
        // this.firstName = firstName;
        // this.lastName = lastName;
        this.username = username;
        this.contactNumber = contactNumber;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    // public String getFirstName() { return firstName; }
    // public void setFirstName(String firstName) { this.firstName = firstName; }

    // public String getLastName() { return lastName; }
    // public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

}

