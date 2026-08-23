package my.maleva.api.module.filehandling.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Outcome of one upload request: what the folder holds now, and what changed. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentUploadResultDto {

    /** Every file in the scope after the request, newest write included. */
    private List<AttachmentDto> attachments;

    /**
     * The {@code path} of each entry in {@link #attachments}, comma-joinable for
     * the legacy {@code FilePath} columns and directly usable by callers that
     * only ever wanted the list of URLs.
     */
    private List<String> paths;

    private int storedCount;

    private int copiedCount;

    private int deletedCount;
}
