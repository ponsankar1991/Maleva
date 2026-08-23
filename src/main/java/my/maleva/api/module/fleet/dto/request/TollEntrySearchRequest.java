package my.maleva.api.module.fleet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Filters for the toll entry list.
 *
 * Mirrors the legacy F5ViewModel fields the screen actually used: Comid,
 * Fromdate, Todate, DId (truck), Employeeid and Search.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollEntrySearchRequest {

    private Integer companyRefId;

    private LocalDate fromDate;
    private LocalDate toDate;

    /** Legacy DId. */
    private Integer truckRefId;

    /** Legacy Employeeid. */
    private Integer employeeRefId;

    /**
     * Toll number (CNumberDisplay). When present the legacy query dropped the
     * date range so a document could be found by number alone; kept.
     */
    private String search;
}
