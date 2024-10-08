package com.example.chat_management.repository;


import com.example.chat_management.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    FileMetadata findByFileName(String fileName);
}
