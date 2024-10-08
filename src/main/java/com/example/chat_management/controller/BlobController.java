package com.example.chat_management.controller;


import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.chat_management.model.FileMetadata;
import com.example.chat_management.repository.FileMetadataRepository;
import com.example.chat_management.service.BlobService;

@RestController
@RequestMapping("/blob")
public class BlobController {

    @Autowired
    private BlobService blobService;

    private final FileMetadataRepository fileMetadataRepository;

    public BlobController(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    // Endpoint for uploading a file
     @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("fileName") String fileName,
                             @RequestParam("senderNumber") String senderNumber,
                             @RequestParam("receiverNumber") String receiverNumber) throws IOException {
        return blobService.uploadFile(file, fileName, senderNumber, receiverNumber);
    }
    // Endpoint for downloading a file
    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileName) {
        byte[] fileData = blobService.downloadFile(fileName);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        return ResponseEntity.ok().headers(headers).body(fileData);
    }

    // Endpoint for deleting a file
    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        try {
            blobService.deleteFile(fileName);
            return ResponseEntity.ok("File deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/metadata")
    public FileMetadata getFileMetadata(@RequestParam("fileName") String fileName) {
        return fileMetadataRepository.findByFileName(fileName);
    }

}
