package my.maleva.api.module.filehandling.service.impl;

import my.maleva.api.common.config.FileUploadConfig;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.filehandling.dto.AttachmentDto;
import my.maleva.api.module.filehandling.dto.AttachmentUploadCommand;
import my.maleva.api.module.filehandling.dto.AttachmentUploadResultDto;
import my.maleva.api.module.filehandling.model.AttachmentScope;
import my.maleva.api.module.filehandling.model.AttachmentUploadMode;
import my.maleva.api.module.filehandling.repository.AttachmentFilePathRepository;
import my.maleva.api.module.filehandling.service.AttachmentStorageService;
import my.maleva.api.module.filehandling.service.ImageCompressor;
import my.maleva.api.module.filehandling.service.PdfPageRasterizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * One implementation of the algorithm the five legacy upload actions each
 * carried a copy of: store what was posted, copy forward what was named,
 * delete what was listed, then report the folder.
 */
@Service
public class AttachmentStorageServiceImpl implements AttachmentStorageService {

    private static final Logger logger = LoggerFactory.getLogger(AttachmentStorageServiceImpl.class);

    private static final String PDF_EXTENSION = ".pdf";

    private final FileUploadConfig config;
    private final ImageCompressor imageCompressor;
    private final PdfPageRasterizer pdfPageRasterizer;
    private final AttachmentFilePathRepository filePathRepository;

    public AttachmentStorageServiceImpl(FileUploadConfig config,
                                        ImageCompressor imageCompressor,
                                        PdfPageRasterizer pdfPageRasterizer,
                                        AttachmentFilePathRepository filePathRepository) {
        this.config = config;
        this.imageCompressor = imageCompressor;
        this.pdfPageRasterizer = pdfPageRasterizer;
        this.filePathRepository = filePathRepository;
    }

    @Override
    public AttachmentUploadResultDto upload(AttachmentUploadCommand command) {
        AttachmentScope scope = command.getScope();
        List<MultipartFile> files = command.getFiles().stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();

        validate(files);

        Path directory = scope.resolveDirectory(config.getStorageRoot());
        int stored = 0;
        int copied = 0;

        try {
            Files.createDirectories(directory);

            if (!files.isEmpty()) {
                for (MultipartFile file : files) {
                    stored += store(file, directory, command);
                }
            } else if (!command.getCopyFromPaths().isEmpty()) {
                // Only when nothing was posted, matching legacy UploadFile3/5:
                // a fresh upload replaces the carried-forward set rather than
                // adding to it.
                copied = copyForward(command.getCopyFromPaths(), directory);
            }

            int deleted = deleteNamed(command.getDeletePaths(), directory);
            return finish(scope, command.getFilePathTable(), stored, copied, deleted);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to store attachments for " + scope, ex);
        }
    }

    @Override
    public List<AttachmentDto> list(AttachmentScope scope) {
        Path directory = scope.resolveDirectory(config.getStorageRoot());
        if (!Files.isDirectory(directory)) {
            return List.of();
        }

        String prefix = scope.publicPathPrefix(config.getPublicUrlPrefix());
        try (Stream<Path> entries = Files.list(directory)) {
            return entries
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(path -> describe(path, prefix))
                    .toList();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to list attachments for " + scope, ex);
        }
    }

    @Override
    public AttachmentUploadResultDto delete(AttachmentScope scope, List<String> paths, String filePathTable) {
        Path directory = scope.resolveDirectory(config.getStorageRoot());
        if (!Files.isDirectory(directory)) {
            return finish(scope, filePathTable, 0, 0, 0);
        }

        try {
            int deleted = deleteNamed(paths, directory);
            return finish(scope, filePathTable, 0, 0, deleted);
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to delete attachments for " + scope, ex);
        }
    }

    /**
     * Writes one upload and returns how many files it produced - more than one
     * when a PDF is rasterised into pages.
     */
    private int store(MultipartFile file, Path directory, AttachmentUploadCommand command) throws IOException {
        String originalName = file.getOriginalFilename();
        String extension = extensionOf(originalName);
        String baseName = command.isKeepOriginalName() ? safeBaseName(originalName) : UUID.randomUUID().toString();
        Path target = directory.resolve(baseName + extension);

        AttachmentUploadMode mode = command.getMode();

        if (mode == AttachmentUploadMode.PDF_AS_IMAGES && PDF_EXTENSION.equals(extension)) {
            file.transferTo(target);
            try {
                return pdfPageRasterizer.rasterize(target, directory, baseName).size();
            } finally {
                Files.deleteIfExists(target);
            }
        }

        boolean compress = mode == AttachmentUploadMode.IMAGES_ONLY || imageCompressor.supports(extension);
        if (compress) {
            try (InputStream source = file.getInputStream()) {
                imageCompressor.compressTo(source, target);
            }
        } else {
            file.transferTo(target);
        }
        return 1;
    }

    /**
     * Copies files already stored elsewhere into this scope, used by the clone
     * and convert screens to carry attachments onto a new record. Each source
     * is resolved back under the storage root, so a caller cannot name a path
     * outside it.
     */
    private int copyForward(List<String> sourcePaths, Path directory) throws IOException {
        int copied = 0;
        for (String sourcePath : sourcePaths) {
            Path source = resolvePublicPath(sourcePath);
            if (source == null || !Files.isRegularFile(source)) {
                logger.debug("Skipping copy-forward of missing attachment {}", sourcePath);
                continue;
            }
            Path target = directory.resolve(source.getFileName().toString());
            if (target.equals(source)) {
                continue;
            }
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            copied++;
        }
        return copied;
    }

    /**
     * Deletes the named files from {@code directory}. Callers send back the
     * public paths they were given, so only the file name is significant; the
     * directory is fixed by the scope and never taken from the caller.
     */
    private int deleteNamed(List<String> paths, Path directory) throws IOException {
        Set<String> names = new LinkedHashSet<>();
        for (String path : paths) {
            String name = fileNameOf(path);
            if (name != null) {
                names.add(name);
            }
        }
        if (names.isEmpty()) {
            return 0;
        }

        int deleted = 0;
        for (String name : names) {
            Path target = directory.resolve(name).normalize();
            if (!target.getParent().equals(directory)) {
                throw new InvalidRequestException("Attachment to delete is outside its folder: " + name);
            }
            if (Files.deleteIfExists(target)) {
                deleted++;
            }
        }
        return deleted;
    }

    private AttachmentUploadResultDto finish(AttachmentScope scope, String filePathTable,
                                             int stored, int copied, int deleted) {
        List<AttachmentDto> attachments = list(scope);
        List<String> paths = attachments.stream().map(AttachmentDto::getPath).toList();

        if (filePathTable != null && !filePathTable.isBlank()) {
            // The files are already on disk by this point, so a failed column
            // sync must not turn a completed write into an error the caller
            // reads as "nothing happened". The legacy service swallowed this
            // the same way; it is logged here rather than discarded silently.
            try {
                filePathRepository.updateFilePath(
                        filePathTable, scope.getRecordId(), scope.getCompanyRefId(), String.join(",", paths));
            } catch (RuntimeException ex) {
                logger.error("Stored attachments for {} but could not update {}.FilePath",
                        scope, filePathTable, ex);
            }
        }

        return AttachmentUploadResultDto.builder()
                .attachments(attachments)
                .paths(paths)
                .storedCount(stored)
                .copiedCount(copied)
                .deletedCount(deleted)
                .build();
    }

    private void validate(List<MultipartFile> files) {
        if (files.size() > config.getMaxFiles()) {
            throw new InvalidRequestException(
                    "Cannot upload more than " + config.getMaxFiles() + " files at once");
        }
        for (MultipartFile file : files) {
            if (file.getSize() > config.getMaxFileSize()) {
                throw new InvalidRequestException(String.format(
                        "%s exceeds the maximum file size of %d MB",
                        file.getOriginalFilename(), config.getMaxFileSize() / (1024 * 1024)));
            }
        }
    }

    private AttachmentDto describe(Path path, String publicPrefix) {
        String name = path.getFileName().toString();
        try {
            return AttachmentDto.builder()
                    .fileName(name)
                    .path(publicPrefix + name)
                    .contentType(Files.probeContentType(path))
                    .sizeBytes(Files.size(path))
                    .lastModified(LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault()))
                    .build();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read attachment " + name, ex);
        }
    }

    @Override
    public java.util.Optional<byte[]> read(String publicPath) {
        Path file = resolvePublicPath(publicPath);
        if (file == null || !Files.isRegularFile(file)) {
            return java.util.Optional.empty();
        }
        try {
            return java.util.Optional.of(Files.readAllBytes(file));
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read attachment " + publicPath, ex);
        }
    }

    /**
     * Maps a public path such as {@code /uploads/6/SalesOrder/12056/a.jpg} back
     * to its file, or null when it does not address the storage root.
     */
    private Path resolvePublicPath(String publicPath) {
        if (publicPath == null || publicPath.isBlank()) {
            return null;
        }

        String prefix = config.getPublicUrlPrefix();
        String relative = publicPath.trim();
        // Match on the prefix as a whole path segment, so "/uploadsomething"
        // is not mistaken for the "/uploads" prefix plus "omething".
        if (relative.equals(prefix)) {
            return null;
        }
        if (relative.startsWith(prefix + "/")) {
            relative = relative.substring(prefix.length());
        }
        relative = relative.replace('\\', '/');
        while (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        if (relative.isEmpty()) {
            return null;
        }

        Path root = config.getStorageRoot();
        Path resolved = root.resolve(relative).normalize();
        if (!resolved.startsWith(root)) {
            throw new InvalidRequestException("Attachment path resolves outside the storage root: " + publicPath);
        }
        return resolved;
    }

    /** Last segment of a stored path, with any directory part discarded. */
    private String fileNameOf(String path) {
        if (path == null) {
            return null;
        }
        String trimmed = path.trim().replace('\\', '/');
        int lastSlash = trimmed.lastIndexOf('/');
        String name = lastSlash >= 0 ? trimmed.substring(lastSlash + 1) : trimmed;
        return name.isEmpty() ? null : name;
    }

    /** Lowercased extension including the dot, or empty when there is none. */
    private String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        String name = fileNameOf(fileName);
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        if (dot <= 0 || dot == name.length() - 1) {
            return "";
        }
        String extension = name.substring(dot).toLowerCase(Locale.ROOT);
        return extension.matches("\\.[a-z0-9]{1,10}") ? extension : "";
    }

    /**
     * The uploaded file's own name, reduced to characters that are safe as a
     * file name. The browser supplies this value, so it can contain directory
     * separators and traversal segments.
     */
    private String safeBaseName(String originalName) {
        String name = fileNameOf(originalName);
        if (name == null) {
            return UUID.randomUUID().toString();
        }
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String sanitized = base.replaceAll("[^A-Za-z0-9._-]", "_").replaceAll("^[._]+", "");
        if (sanitized.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sanitized.length() > 100 ? sanitized.substring(0, 100) : sanitized;
    }
}
