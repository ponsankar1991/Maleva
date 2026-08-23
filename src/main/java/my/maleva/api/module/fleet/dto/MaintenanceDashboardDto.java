package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Everything the maintenance dashboard shows in one call. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceDashboardDto {

    /** The day the figures were computed for. */
    private LocalDate asOf;

    /** How far ahead the alerts look. Anything further away is not reported. */
    private Integer horizonDays;

    /** Inside this many days an alert counts as critical rather than a warning. */
    private Integer criticalDays;

    private Integer activeTrucks;
    private Integer activeDrivers;

    /** Already past their date. */
    private Integer expiredCount;
    /** Due within criticalDays. */
    private Integer criticalCount;
    /** Due within horizonDays but beyond criticalDays. */
    private Integer warningCount;

    /** Trucks with at least one expired or upcoming item. */
    private Integer trucksNeedingAttention;
    private Integer driversNeedingAttention;

    /** How many alerts fall in each category, for the breakdown panel. */
    private Map<String, Integer> byCategory;

    /** Every alert, most urgent first. */
    private List<ExpiryAlertDto> alerts;

    /** Workshop jobs that are neither Completed nor Cancelled. */
    private Integer openJobCount;

    /** Of those, how many are past their expected completion date. */
    private Integer overdueJobCount;

    /** The open jobs themselves, most overdue first. */
    private List<OpenJobOrderDto> openJobs;
}
