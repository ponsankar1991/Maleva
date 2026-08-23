package my.maleva.api.module.filehandling.controller;

import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.filehandling.dto.AttachmentDto;
import my.maleva.api.module.filehandling.dto.AttachmentUploadCommand;
import my.maleva.api.module.filehandling.dto.AttachmentUploadResultDto;
import my.maleva.api.module.filehandling.model.AttachmentScope;
import my.maleva.api.module.filehandling.model.AttachmentUploadMode;
import my.maleva.api.module.filehandling.service.AttachmentStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Attachments of a business record, addressed by company, folder and record id.
 *
 * This is the endpoint new screens should use. The legacy numbered actions are
 * served by {@link LegacyAttachmentController} against the same service, so
 * both routes see one set of files.
 */
@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentStorageService attachmentStorageService;

    public AttachmentController(AttachmentStorageService attachmentStorageService) {
        this.attachmentStorageService = attachmentStorageService;
    }

    /**
     * Stores files against a record, and in the same request removes any
     * {@code deletePaths} the screen staged. Posting no files with only
     * {@code copyFromPaths} carries an existing record's attachments onto this
     * one.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AttachmentUploadResultDto>> upload(
            @RequestParam Integer companyRefId,
            @RequestParam Integer recordId,
            @RequestParam String folderName,
            @RequestParam(required = false) String subFolderName,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false, defaultValue = "false") boolean keepOriginalName,
            @RequestParam(required = false) List<String> deletePaths,
            @RequestParam(required = false) List<String> copyFromPaths,
            @RequestParam(required = false) String filePathTable,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        AttachmentUploadCommand command = AttachmentUploadCommand.builder()
                .scope(AttachmentScope.of(companyRefId, folderName, recordId, subFolderName))
                .mode(AttachmentUploadMode.parse(mode))
                .files(files == null ? List.of() : files)
                .keepOriginalName(keepOriginalName)
                .deletePaths(deletePaths == null ? List.of() : deletePaths)
                .copyFromPaths(copyFromPaths == null ? List.of() : copyFromPaths)
                .filePathTable(filePathTable)
                .build();

        AttachmentUploadResultDto result = attachmentStorageService.upload(command);
        return ResponseEntity.ok(ApiResponse.success(result, "Attachments saved"));
    }

    /** Files currently attached to a record. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AttachmentDto>>> list(
            @RequestParam Integer companyRefId,
            @RequestParam Integer recordId,
            @RequestParam String folderName,
            @RequestParam(required = false) String subFolderName) {

        List<AttachmentDto> attachments = attachmentStorageService.list(
                AttachmentScope.of(companyRefId, folderName, recordId, subFolderName));
        return ResponseEntity.ok(ApiResponse.success(attachments, "Attachments fetched"));
    }

    /** Removes the named attachments and returns what remains. */
    @DeleteMapping
    public ResponseEntity<ApiResponse<AttachmentUploadResultDto>> delete(
            @RequestParam Integer companyRefId,
            @RequestParam Integer recordId,
            @RequestParam String folderName,
            @RequestParam(required = false) String subFolderName,
            @RequestParam List<String> paths,
            @RequestParam(required = false) String filePathTable) {

        AttachmentUploadResultDto result = attachmentStorageService.delete(
                AttachmentScope.of(companyRefId, folderName, recordId, subFolderName), paths, filePathTable);
        return ResponseEntity.ok(ApiResponse.success(result, "Attachments deleted"));
    }
}
