package my.maleva.api.module.filehandling.service;

import my.maleva.api.common.config.FileUploadConfig;
import my.maleva.api.module.filehandling.dto.FileUploadResponseDto;
import my.maleva.api.common.exception.InvalidRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    private final FileUploadConfig fileUploadConfig;

    public FileUploadService(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
    }

    /**
     * Upload a single file
     */
    public FileUploadResponseDto uploadFile(MultipartFile file) {
        validateFile(file);
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            Path uploadPath = getUploadPath();
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            return FileUploadResponseDto.builder()
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .success(true)
                    .message("File uploaded successfully")
                    .uploadTime(LocalDateTime.now())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Upload multiple files
     */
    public List<FileUploadResponseDto> uploadFiles(MultipartFile[] files) {
        if (files.length > fileUploadConfig.getMaxFiles()) {
            throw new InvalidRequestException("Cannot upload more than " + fileUploadConfig.getMaxFiles() + " files at once");
        }

        List<FileUploadResponseDto> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(uploadFile(file));
        }
        return responses;
    }

    /**
     * Upload file to a specific directory
     */
    public FileUploadResponseDto uploadFileToDirectory(MultipartFile file, String directory) {
        validateFile(file);
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            Path uploadPath = getUploadPath().resolve(directory);
            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, file.getBytes());

            return FileUploadResponseDto.builder()
                    .fileName(fileName)
                    .originalFileName(file.getOriginalFilename())
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .success(true)
                    .message("File uploaded successfully")
                    .uploadTime(LocalDateTime.now())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file
     */
    public boolean deleteFile(String fileName) {
        try {
            Path filePath = getUploadPath().resolve(fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from a specific directory
     */
    public boolean deleteFileFromDirectory(String directory, String fileName) {
        try {
            Path filePath = getUploadPath().resolve(directory).resolve(fileName);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    /**
     * Validate file size and existence
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("File is empty or null");
        }

        if (file.getSize() > fileUploadConfig.getMaxFileSize()) {
            throw new InvalidRequestException(
                    String.format("File size exceeds maximum limit of %d MB",
                            fileUploadConfig.getMaxFileSize() / (1024 * 1024))
            );
        }
    }

    /**
     * Generate unique file name
     */
    private String generateFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Get upload path
     */
    private Path getUploadPath() {
        return Paths.get(fileUploadConfig.getUploadDir()).toAbsolutePath().normalize();
    }
}

