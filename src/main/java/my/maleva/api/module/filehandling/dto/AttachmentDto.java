package my.maleva.api.module.filehandling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** One stored file, as the UI needs to list, preview and delete it. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentDto {

    /** File name on disk, e.g. {@code 9f3c...-a1.jpg}. */
    private String fileName;

    /**
     * Browser-resolvable path, e.g. {@code /uploads/6/SalesOrder/12056/9f3c.jpg}.
     * This is also the value persisted in the legacy {@code FilePath} columns and
     * the value callers send back to delete the file.
     */
    private String path;

    private String contentType;

    private long sizeBytes;

    private LocalDateTime lastModified;
}
