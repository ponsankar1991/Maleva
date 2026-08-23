package my.maleva.api.module.fleet.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * A toll entry being created or updated, with all of its transactions.
 *
 * Master and details are saved together in one call, because SP_TollEntry
 * replaces the whole detail set: it deletes every TollEntryDetails row of the
 * master before inserting what it was given. Sending a partial list would
 * silently drop the rest.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollEntrySaveRequest {

    /** Null or 0 creates; anything else updates that entry. */
    private Integer id;

    @NotNull(message = "Company is required")
    private Integer companyRefId;

    @NotNull(message = "Truck is required")
    private Integer truckRefId;

    private Integer employeeRefId;

    @NotNull(message = "Toll date is required")
    private LocalDate saleDate;

    /**
     * Header total. Recomputed from the detail lines on save, so whatever the
     * client sends here is only a hint.
     */
    @PositiveOrZero(message = "Amount cannot be negative")
    private Double amount;

    @Size(max = 2000, message = "Remarks must not exceed 2000 characters")
    private String remarks;

    private String filePath;

    @Valid
    private List<TollEntryDetailRequest> details;
}
