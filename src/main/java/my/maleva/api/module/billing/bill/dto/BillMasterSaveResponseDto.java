package my.maleva.api.module.billing.bill.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a bill save.
 *
 * <p>{@code billNoDisplay} is the assigned document number, shown on screen
 * after an insert.
 *
 * <p>{@code id} is what the follow-up file upload attaches to, and it must be
 * read from here rather than guessed: bill attachments deliberately still go
 * to the legacy .NET host (<code>/Common/UploadFile3</code> with
 * <code>FolderName: "BillMaster"</code>), because the old system is still in
 * use and both front ends have to read the same document folder. Only the
 * payment screen has been moved to the Spring attachment endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillMasterSaveResponseDto {

    private boolean success;
    private String message;
    private Integer id;
    private String billNoDisplay;

    /**
     * True when this save was a repeat of one already stored — a double-click,
     * a retry, a second tab — and no new bill was entered. The response still
     * carries the original bill's id and number, so the screen can carry on
     * (attach its files, show the number) exactly as it would after a first
     * save. Worth surfacing so the clerk knows why the number looks familiar.
     */
    private boolean duplicate;
}
