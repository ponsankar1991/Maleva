package my.maleva.api.module.fleet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Filters for the truck order calendar.
 *
 * Mirrors the legacy TruckOrderFilterViewModel, minus {@code ClientRefId} which
 * the model declared and no query ever read.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckOrderSearchRequest {

    private Integer companyRefId;

    private LocalDate fromDate;
    private LocalDate toDate;

    /** Legacy TruckRefId; 0 or null means every truck. */
    private Integer truckRefId;

    /**
     * Legacy Status, now a list because the combo behind it was multi-select.
     * Empty means every status.
     */
    private List<String> statuses;
}
