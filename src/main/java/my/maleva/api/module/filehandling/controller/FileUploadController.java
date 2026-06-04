package my.maleva.api.module.filehandling.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.constant.SecurityConstants;
import my.maleva.api.module.filehandling.dto.FileUploadResponseDto;
import my.maleva.api.module.filehandling.service.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/file-uploads")
@Validated
@PermitAll
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Upload a single file
     * POST /api/file-uploads
     */
    @PostMapping
    public ResponseEntity<FileUploadResponseDto> uploadFile(@NotNull @RequestParam("file") MultipartFile file) {
        FileUploadResponseDto response = fileUploadService.uploadFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Upload multiple files
     * POST /api/file-uploads/multiple
     */
    @PostMapping("/multiple")
    public ResponseEntity<List<FileUploadResponseDto>> uploadMultipleFiles(
            @NotNull @RequestParam("files") MultipartFile[] files) {
        List<FileUploadResponseDto> responses = fileUploadService.uploadFiles(files);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * Upload file to a specific directory
     * POST /api/file-uploads/directory/{directory}
     */
    @PostMapping("/directory/{directory}")
    public ResponseEntity<FileUploadResponseDto> uploadFileToDirectory(
            @NotNull @RequestParam("file") MultipartFile file,
            @PathVariable String directory) {
        FileUploadResponseDto response = fileUploadService.uploadFileToDirectory(file, directory);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete a file
     * DELETE /api/file-uploads/{fileName}
     */
    @DeleteMapping("/{fileName}")
    public ResponseEntity<Void> deleteFile(@PathVariable String fileName) {
        fileUploadService.deleteFile(fileName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete a file from a specific directory
     * DELETE /api/file-uploads/directory/{directory}/{fileName}
     */
    @DeleteMapping("/directory/{directory}/{fileName}")
    public ResponseEntity<Void> deleteFileFromDirectory(
            @PathVariable String directory,
            @PathVariable String fileName) {
        fileUploadService.deleteFileFromDirectory(directory, fileName);
        return ResponseEntity.noContent().build();
    }
}

