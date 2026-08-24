package my.maleva.api.module.fleet.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Filters for the levi entry list.
 *
 * The legacy screen posted these as {@code F5ViewModel}, a shared bag of
 * single-letter fields where {@code DId} meant the truck, {@code TId} the
 * driver, {@code Id1} the RTI, and {@code Offvesselname}/{@code Loadingvesselname}
 * carried the enter and exit links. Each one is named for what it filters here.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassEntrySearchRequest {

    private Integer companyRefId;

    /** Ignored when {@link #search} is set, matching the legacy behaviour. */
    private LocalDate fromDate;

    private LocalDate toDate;

    /** Legacy {@code DId}. */
    private Integer truckRefId;

    /** Legacy {@code TId}. */
    private Integer driverRefId;

    /** Legacy {@code Id1}. */
    private Integer rtiRefId;

    private Integer employeeRefId;

    /** Legacy {@code Offvesselname}. One of {@code IN} / {@code OUT}. */
    private String enterLink;

    /** Legacy {@code Loadingvesselname}. One of {@code 1ST LINK} / {@code 2ND LINK}. */
    private String exitLink;

    /**
     * Exact levi number, e.g. {@code LE000000241}. When present it replaces
     * every other filter including the date range, which is how the legacy
     * "search by number" box behaved.
     */
    private String search;
}
