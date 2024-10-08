package com.example.chat_management.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.example.chat_management.model.FileMetadata;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.example.chat_management.repository.FileMetadataRepository;

import java.io.IOException;

@Service
public class BlobService {

    // Inject the SAS URL and Token from properties or hardcode
    @Value("${azure.blob.sas-url}")
    private String blobSasUrl;

    @Value("${azure.blob.sas-token}")
    private String blobSasToken;

    private final FileMetadataRepository fileMetadataRepository;

    public BlobService(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    // Upload a file to Azure Blob Storage
    public String uploadFile(MultipartFile file, String fileName, String senderNumber, String receiverNumber) throws IOException {
        String blobUrlWithSas = blobSasToken;

        

        // Create a BlobClient to interact with the blob
        BlobClient blobClient = new BlobClientBuilder()
                .endpoint(blobUrlWithSas)
                .blobName(fileName)
                .buildClient();

        // Upload file
        blobClient.upload(file.getInputStream(), file.getSize(), true);

        // Save metadata including the Blob URL
        FileMetadata metadata = new FileMetadata();
        metadata.setSenderNumber(senderNumber);
        metadata.setReceiverNumber(receiverNumber);
        metadata.setFileName(fileName);
        metadata.setFileType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setBlobUrl(blobClient.getBlobUrl()); // Store the blob URL
        
        fileMetadataRepository.save(metadata);  // Save metadata to the database
        
        return blobClient.getBlobUrl();  // return URL of the uploaded blob
    }

    // Download a file from Azure Blob Storage
    public byte[] downloadFile(String fileName) {
        String blobUrlWithSas = blobSasToken;

        // Create a BlobClient to interact with the blob
        BlobClient blobClient = new BlobClientBuilder()
                .endpoint(blobUrlWithSas)
                .blobName(fileName)
                .buildClient();

        // Download the content as bytes
        return blobClient.downloadContent().toBytes();
    }

    // Delete a file from Azure Blob Storage
    public void deleteFile(String fileName) {
        String blobUrlWithSas = blobSasToken;

        // Create a BlobClient to interact with the blob
        BlobClient blobClient = new BlobClientBuilder()
                .endpoint(blobUrlWithSas)
                .blobName(fileName)
                .buildClient();

        // Delete the blob
        blobClient.delete();
    }
}
