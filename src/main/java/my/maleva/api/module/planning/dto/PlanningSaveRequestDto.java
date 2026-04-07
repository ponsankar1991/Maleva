package my.maleva.api.module.planning.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for Planning Insert/Update operations.
 * Matches the frontend payload from VesselPlanningViewPage.tsx
 *
 * Frontend sends: { Id, PlaningNo, PlaningDate, Remarks, Comid, Details[] }
 * Backend stores: { id, cNumberDisplay, saleDate, remarks, companyRefId, planningDetails[] }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningSaveRequestDto {

    /**
     * Planning ID - 0 for new insert, >0 for update
     */
    @NotNull(message = "ID is required")
    private Integer id;

    /**
     * Company ID from request header (Comid)
     */
    @NotNull(message = "Company reference ID is required")
    private Integer comid;

    /**
     * Planning number display (e.g., "PL000000001")
     * Frontend generates this via /max-planning-no endpoint
     */
    @NotBlank(message = "Planning number is required")
    private String planingNo;

    /**
     * Planning date
     */
    @NotNull(message = "Planning date is required")
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate planingDate;

    /**
     * From date filter
     */
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate fromDate;

    /**
     * To date filter
     */
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate toDate;

    /**
     * Search text (comma-separated port codes)
     */
    private String searchText;

    /**
     * Remarks
     */
    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    /**
     * Employee reference ID
     */
    private Integer employeeRefId;

    /**
     * User reference ID
     */
    private Integer userRefId;

    /**
     * Planning details (jobs to be planned)
     */
    @Valid
    private List<PlanningDetailItemDto> details;

    /**
     * Inner DTO for each planning detail item (job)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanningDetailItemDto {

        private Integer id;

        /**
         * Sort order
         */
        @NotNull(message = "Sort order is required")
        private Integer sortBy;

        /**
         * Sale order master reference ID (the job being planned)
         */
        @NotNull(message = "Sale order reference ID is required")
        private Integer saleOrderMasterRefId;

        /**
         * Truck reference ID (optional)
         */
        private Integer truckRefId;

        /**
         * Remarks for this detail
         */
        @Size(max = 300)
        private String remarks;

        /**
         * Origin (display)
         */
        @Size(max = 150)
        private String originD;

        /**
         * Destination (display)
         */
        @Size(max = 150)
        private String destinationD;

        /**
         * Pickup date
         */
        @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
        private LocalDateTime pickupDateD;

        /**
         * Delivery date
         */
        @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
        private LocalDateTime deliveryDateD;

        /**
         * Pickup time list (comma-separated)
         */
        @Size(max = 500)
        private String pickupTimeList;

        /**
         * Pickup quantity list (comma-separated)
         */
        @Size(max = 500)
        private String pickupQuantityList;

        /**
         * Delivery quantity list (comma-separated)
         */
        @Size(max = 500)
        private String deliveryQuantityList;

        /**
         * Delivery time list (comma-separated)
         */
        @Size(max = 500)
        private String deliveryTimeList;

        /**
         * Truck name (display)
         */
        @Size(max = 200)
        private String truckNameD;

        /**
         * Driver name (display)
         */
        @Size(max = 200)
        private String driverNameD;

        // Additional fields from frontend (for reference/joins)
        private String jobNo;
        private String jobDate;
        private String customerName;
        private String origin;
        private String destination;
        private String vessel;
        private String eta;
        private String etb;
        private String etd;
        private String leta;
        private String oeta;
        private String oetb;
        private String oetd;
        private String jobStatus;
        private String commodity;
        private String pkg;
        private String cargo;
        private String ptw;
        private Integer boardingOfficerRefid;
        private String boardingOfficerName;
        private Integer boardingOfficer1Refid;
        private String boardingOfficerName1;
        private Integer boardingAmount;
        private Integer boardingAmount1;
        private String agentName;
        private String agentPhone;
    }
}
