package com.example.chat_management.repository;

import com.example.chat_management.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    Optional<UserStatus> findByUserNumber(String userNumber);
}
