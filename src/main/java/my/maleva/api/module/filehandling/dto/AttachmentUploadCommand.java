package my.maleva.api.module.filehandling.dto;

import lombok.Builder;
import lombok.Getter;
import my.maleva.api.module.filehandling.model.AttachmentScope;
import my.maleva.api.module.filehandling.model.AttachmentUploadMode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Everything one upload request asks for, assembled by the controller so the
 * service never touches {@code HttpServletRequest} headers.
 *
 * The legacy actions took the same information as six loosely-typed headers,
 * each read inside its own empty {@code try/catch} so a malformed value became
 * a silent zero. Parsing happens once, at the edge, and a bad value is now a
 * 400 instead of a write into company 0.
 */
@Getter
@Builder
public class AttachmentUploadCommand {

    private final AttachmentScope scope;

    private final AttachmentUploadMode mode;

    /** Files posted with this request; never null, may be empty. */
    @Builder.Default
    private final List<MultipartFile> files = List.of();

    /**
     * Keep each uploaded file's own name instead of assigning a UUID. Mirrors
     * the legacy {@code FileName} header, which was only ever used as a flag -
     * its value was discarded and the browser-supplied name won.
     */
    private final boolean keepOriginalName;

    /**
     * Paths to remove from the scope, as previously returned by an upload or
     * list call. Mirrors the legacy {@code DeleteFileName} header.
     */
    @Builder.Default
    private final List<String> deletePaths = List.of();

    /**
     * Paths of files already stored under another scope, to be copied into this
     * one when no new files are posted. Mirrors the legacy
     * {@code ExistingFilePath} header, which the clone/convert screens use to
     * carry attachments onto the new record.
     */
    @Builder.Default
    private final List<String> copyFromPaths = List.of();

    /**
     * When set, the resulting path list is written to {@code FilePath} on this
     * table for the scope's record. Legacy {@code UploadFile} always did this,
     * using the folder name as the table name; the two are now separate so a
     * folder rename cannot silently retarget an UPDATE.
     */
    private final String filePathTable;
}
