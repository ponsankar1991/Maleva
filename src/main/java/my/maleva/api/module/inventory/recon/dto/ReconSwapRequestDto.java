package my.maleva.api.module.inventory.recon.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Remove the failed unit from a truck and fit its replacement, as one act.
 *
 * The two legs are deliberately one request. Issued as separate calls, a
 * failure between them leaves the truck recorded as having neither the old
 * part nor the new one.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconSwapRequestDto {

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotNull(message = "Truck is required - a recon job records which truck the unit came off")
    private Integer truckRefId;

    /** The workshop job order this swap belongs to, when there is one. */
    private Integer jobOrderRefId;

    @NotNull(message = "Details of the removed unit are required")
    @Valid
    private RemovedUnit removed;

    /**
     * The replacement. Optional: a unit is sometimes pulled off without one
     * being fitted straight away, and the truck is simply left short.
     */
    @Valid
    private FittedUnit fitted;

    @NotBlank(message = "Performed By is required")
    @Size(max = 50, message = "Performed By cannot exceed 50 characters")
    private String performedBy;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RemovedUnit {

        @NotNull(message = "Product of the removed unit is required")
        private Integer productRefId;

        @NotBlank(message = "Serial No of the removed unit is required")
        @Size(max = 100, message = "Serial No cannot exceed 100 characters")
        private String serialNo;

        @Size(max = 500, message = "Fault description cannot exceed 500 characters")
        private String faultDescription;

        /**
         * What a new replacement costs. Recorded on the job so the report can
         * put the repair spend next to it and show what reconditioning saved.
         */
        @DecimalMin(value = "0.0", message = "New part cost cannot be negative")
        private BigDecimal newPartCost;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FittedUnit {

        @NotNull(message = "Product of the fitted unit is required")
        private Integer productRefId;

        @NotBlank(message = "Serial No of the fitted unit is required")
        @Size(max = 100, message = "Serial No cannot exceed 100 characters")
        private String serialNo;
    }
}
