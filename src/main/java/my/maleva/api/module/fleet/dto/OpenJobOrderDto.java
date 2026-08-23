package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A workshop job that is still open.
 *
 * "Open" means the job has not been Completed or Cancelled - so both `assign`
 * and `InProgress` count. Filtering on InProgress alone would show nothing
 * today: every open job on this data sits at `assign`, because InProgress is
 * marked inactive in JobOrderStatusMaster and cannot currently be selected.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpenJobOrderDto {

    private Integer id;
    private String jobNo;

    private Integer truckRefId;
    private String truckName;

    private Integer statusRefId;
    /** assign or InProgress. */
    private String statusName;
    private String priorityName;

    private String vendorName;
    private String problemName;

    private LocalDate jobDate;
    private LocalDate expectedCompletionDate;

    /**
     * Days until the expected completion date. Negative once it is past.
     * Null when no completion date was set.
     */
    private Integer daysRemaining;

    /** True when the expected completion date has gone by. */
    private boolean overdue;

    private BigDecimal estimatedCost;
}
