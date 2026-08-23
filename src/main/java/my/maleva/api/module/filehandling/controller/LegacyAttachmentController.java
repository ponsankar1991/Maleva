package my.maleva.api.module.filehandling.controller;

import jakarta.servlet.http.HttpServletRequest;
import my.maleva.api.module.filehandling.dto.AttachmentUploadCommand;
import my.maleva.api.module.filehandling.dto.AttachmentUploadResultDto;
import my.maleva.api.module.filehandling.model.AttachmentScope;
import my.maleva.api.module.filehandling.model.AttachmentUploadMode;
import my.maleva.api.module.filehandling.service.AttachmentStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The legacy {@code /Common/UploadFile*} routes, kept alive for screens that
 * have not moved to {@link AttachmentController} yet.
 *
 * Every action here delegates to {@link AttachmentStorageService}: the numbered
 * suffix chooses an {@link AttachmentUploadMode} and nothing else. Both the
 * {@code /api}-prefixed and bare paths are registered because the React app
 * currently calls each in different features.
 *
 * The response keeps the legacy {@code {ok, data, message}} envelope, and
 * {@code data} keeps being a bare list of path strings, so existing callers do
 * not have to change with the endpoint.
 */
@RestController
@RequestMapping({"/api/Common", "/Common"})
public class LegacyAttachmentController {

    private static final String IMAGES_PART_PREFIX = "MyImages";

    private final AttachmentStorageService attachmentStorageService;

    public LegacyAttachmentController(AttachmentStorageService attachmentStorageService) {
        this.attachmentStorageService = attachmentStorageService;
    }

    /**
     * Compresses every upload and writes the resulting paths to the record's
     * {@code FilePath} column - the only legacy action that touched the
     * database, using its {@code FolderName} header as the table name.
     */
    @PostMapping("/UploadFile")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestHeader(value = "Comid", required = false) Integer comId,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "FileName", required = false) String fileName,
            @RequestHeader(value = "DeleteFileName", required = false) String deleteFileName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName,
            HttpServletRequest request) {

        return handleUpload("UploadFile", comId, id, folderName, fileName, deleteFileName,
                subFolderName, null, folderName, request);
    }

    /** Compresses images, stores documents as they arrived. */
    @PostMapping("/UploadFile2")
    public ResponseEntity<Map<String, Object>> uploadFile2(
            @RequestHeader(value = "Comid", required = false) Integer comId,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "FileName", required = false) String fileName,
            @RequestHeader(value = "DeleteFileName", required = false) String deleteFileName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName,
            HttpServletRequest request) {

        return handleUpload("UploadFile2", comId, id, folderName, fileName, deleteFileName,
                subFolderName, null, null, request);
    }

    /** {@code UploadFile2} plus carrying an existing record's files forward. */
    @PostMapping("/UploadFile3")
    public ResponseEntity<Map<String, Object>> uploadFile3(
            @RequestHeader(value = "Comid", required = false) Integer comId,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "FileName", required = false) String fileName,
            @RequestHeader(value = "DeleteFileName", required = false) String deleteFileName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName,
            @RequestHeader(value = "ExistingFilePath", required = false) String existingFilePath,
            HttpServletRequest request) {

        return handleUpload("UploadFile3", comId, id, folderName, fileName, deleteFileName,
                subFolderName, existingFilePath, null, request);
    }

    /** {@code UploadFile3} plus rasterising each PDF into one image per page. */
    @PostMapping("/UploadFile5")
    public ResponseEntity<Map<String, Object>> uploadFile5(
            @RequestHeader(value = "Comid", required = false) Integer comId,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "FileName", required = false) String fileName,
            @RequestHeader(value = "DeleteFileName", required = false) String deleteFileName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName,
            @RequestHeader(value = "ExistingFilePath", required = false) String existingFilePath,
            HttpServletRequest request) {

        return handleUpload("UploadFile5", comId, id, folderName, fileName, deleteFileName,
                subFolderName, existingFilePath, null, request);
    }

    /** Lists the files attached to a record. */
    @PostMapping("/FetchFile2")
    public ResponseEntity<Map<String, Object>> fetchFile2(
            @RequestHeader(value = "Comid", required = false) Integer comId,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName) {

        AttachmentScope scope = AttachmentScope.of(comId, folderName, id, subFolderName);
        List<String> paths = attachmentStorageService.list(scope).stream()
                .map(attachment -> attachment.getPath())
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("ok", true);
        body.put("message", "Fetched Successfully");
        // Both spellings: the legacy action returned "Data", several callers read "data".
        body.put("Data", paths);
        body.put("data", paths);
        return ResponseEntity.ok(body);
    }

    /** Deletes the named files and resyncs the record's {@code FilePath} column. */
    @PostMapping("/DeleteFile")
    public ResponseEntity<Map<String, Object>> deleteFile(
            @RequestHeader(value = "Comid", required = false) Integer comId,
            @RequestHeader(value = "Id", required = false) Integer id,
            @RequestHeader(value = "FolderName", required = false) String folderName,
            @RequestHeader(value = "SubFolderName", required = false) String subFolderName,
            @RequestHeader(value = "FileName", required = false) String fileName) {

        AttachmentScope scope = AttachmentScope.of(comId, folderName, id, subFolderName);
        AttachmentUploadResultDto result =
                attachmentStorageService.delete(scope, splitPaths(fileName), folderName);

        Map<String, Object> body = new HashMap<>();
        body.put("ok", result.getDeletedCount() > 0);
        body.put("data", result.getPaths());
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> handleUpload(
            String legacyAction, Integer comId, Integer id, String folderName, String fileNameFlag,
            String deleteFileName, String subFolderName, String existingFilePath,
            String filePathTable, HttpServletRequest request) {

        List<MultipartFile> files = collectImageParts(request);

        AttachmentUploadCommand command = AttachmentUploadCommand.builder()
                .scope(AttachmentScope.of(comId, folderName, id, subFolderName))
                .mode(AttachmentUploadMode.fromLegacyAction(legacyAction))
                .files(files)
                // The legacy header was only ever a flag: its value was thrown
                // away and the browser-supplied file name won.
                .keepOriginalName(fileNameFlag != null && !fileNameFlag.isBlank())
                .deletePaths(splitPaths(deleteFileName))
                .copyFromPaths(splitPaths(existingFilePath))
                .filePathTable(filePathTable)
                .build();

        AttachmentUploadResultDto result = attachmentStorageService.upload(command);
        boolean changed = result.getStoredCount() > 0
                || result.getCopiedCount() > 0
                || result.getDeletedCount() > 0;

        Map<String, Object> body = new HashMap<>();
        body.put("ok", changed);
        body.put("data", result.getPaths());
        body.put("message", changed ? "Uploaded Successfully" : "No files uploaded or copied");
        return ResponseEntity.ok(body);
    }

    /**
     * Collects the {@code MyImages0..MyImagesN} parts in index order.
     *
     * The first Java port declared ten {@code @RequestParam} arguments and so
     * silently dropped an eleventh file; reading the parts off the request keeps
     * the legacy behaviour of accepting however many the screen sent, with the
     * real cap coming from the configured {@code file.upload.max-files}.
     */
    private List<MultipartFile> collectImageParts(HttpServletRequest request) {
        if (!(request instanceof MultipartHttpServletRequest multipartRequest)) {
            return List.of();
        }

        List<String> partNames = new ArrayList<>();
        multipartRequest.getFileNames().forEachRemaining(name -> {
            if (name.startsWith(IMAGES_PART_PREFIX)) {
                partNames.add(name);
            }
        });
        partNames.sort(Comparator.comparingInt(this::partIndex).thenComparing(name -> name));

        List<MultipartFile> files = new ArrayList<>();
        for (String partName : partNames) {
            files.addAll(multipartRequest.getFiles(partName));
        }
        return files;
    }

    private int partIndex(String partName) {
        String suffix = partName.substring(IMAGES_PART_PREFIX.length());
        try {
            return Integer.parseInt(suffix);
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    /** Splits a legacy comma-joined header into paths, dropping blanks. */
    private List<String> splitPaths(String joined) {
        if (joined == null || joined.isBlank()) {
            return List.of();
        }
        return Arrays.stream(joined.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
