package my.maleva.api.module.filehandling.service;

import my.maleva.api.module.filehandling.dto.AttachmentDto;
import my.maleva.api.module.filehandling.dto.AttachmentUploadCommand;
import my.maleva.api.module.filehandling.dto.AttachmentUploadResultDto;
import my.maleva.api.module.filehandling.model.AttachmentScope;

import java.util.List;

/**
 * Stores, lists and removes the files attached to a business record.
 *
 * Replaces the five legacy {@code CommonController} actions
 * ({@code UploadFile}, {@code UploadFile2}, {@code UploadFile3},
 * {@code UploadFile5}, {@code FetchFile2}), which shared one algorithm and
 * differed only in which file types they compressed and whether they could copy
 * attachments forward from another record.
 */
public interface AttachmentStorageService {

    /**
     * Applies one request: stores posted files, copies forward any
     * {@code copyFromPaths} when nothing was posted, removes any
     * {@code deletePaths}, and reports the folder's resulting contents.
     */
    AttachmentUploadResultDto upload(AttachmentUploadCommand command);

    /** Files currently stored for {@code scope}, sorted by name. Empty if the folder does not exist. */
    List<AttachmentDto> list(AttachmentScope scope);

    /**
     * Removes {@code paths} from {@code scope} and reports what remains.
     *
     * @param filePathTable optional table whose {@code FilePath} column is resynced; may be null
     */
    AttachmentUploadResultDto delete(AttachmentScope scope, List<String> paths, String filePathTable);
}
