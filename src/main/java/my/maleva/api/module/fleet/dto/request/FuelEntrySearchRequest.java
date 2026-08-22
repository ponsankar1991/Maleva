package my.maleva.api.module.fleet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Filters for the fuel entry list, the screen the legacy code reached with F5.
 *
 * Mirrors the legacy F5ViewModel fields: Comid, Fromdate, Todate, DId (truck),
 * TId (driver), Employeeid and Search. The legacy service pasted each of these
 * straight into a WHERE clause; here they are bound parameters.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelEntrySearchRequest {

    private Integer companyRefId;

    /** Start of the SaleDate range, inclusive. Ignored when search is set. */
    private LocalDate fromDate;

    /** End of the SaleDate range, inclusive. Ignored when search is set. */
    private LocalDate toDate;

    /** Legacy DId. */
    private Integer truckRefId;

    /** Legacy TId. */
    private Integer driverRefId;

    /** Legacy Employeeid, set when the "my entries only" box is ticked. */
    private Integer employeeRefId;

    /**
     * Fuel number (CNumberDisplay). When present the legacy query dropped every
     * other filter including the date range, so an entry can be found by number
     * alone; that behaviour is kept.
     */
    private String search;
}
