package my.maleva.api.module.ir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * The IR list filters. Every field except {@code companyRefId} is optional; an
 * absent or blank one is not applied at all rather than matching nothing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrSearchRequest {

    private Integer companyRefId;

    /** Inclusive date range over IRDate. */
    private LocalDate fromDate;
    private LocalDate toDate;

    private Integer irStatusRefId;

    /** UserRoles.roleId. */
    private Integer departmentRefId;

    /** Contains-match on the stored snapshot, for rows whose role id has moved on. */
    private String departmentName;

    private String vesselName;

    private Integer truckRefId;

    /** Contains-match on the plate snapshot, so a hired lorry with no TruckRefId is findable. */
    private String truckNo;

    private Integer employeeRefId;

    private Integer driverRefId;

    /** Contains-match over description, reason, vessel, truck no, driver and employee. */
    private String search;

    /**
     * Only IRs that are not finished yet. Resolved against the finished status
     * codes rather than a hardcoded id, so it survives a re-seeded lookup table.
     */
    private Boolean openOnly;

    /** Set by the service, never by the caller: ids of the finished statuses. */
    private List<Integer> excludeStatusRefIds;
}
