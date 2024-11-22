package com.example.chat_management.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.chat_management.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByContactNumber(String contactNumber);
    boolean existsByUsername(String username);
    boolean existsByContactNumber(String contact);
    Optional<User> findByUsername(String username);
}
