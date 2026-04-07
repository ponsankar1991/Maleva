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
 * Request DTO for Planning Insert/Update.
 * Matches .NET PLANINGMasterModel
 *
 * Frontend sends List<PlanningRequest> (wrapped in JSON array)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningRequest {

    /**
     * ID = 0 for INSERT, ID > 0 for UPDATE
     */
    private Integer id;

    /**
     * Company reference ID
     */
    @NotNull(message = "Company reference ID is required")
    private Integer companyRefId;

    /**
     * User reference ID (from auth)
     */
    private Integer userRefId;

    /**
     * Employee reference ID
     */
    private Integer employeeRefId;

    /**
     * From Date
     */
    @NotNull(message = "From date is required")
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate fDate;

    /**
     * To Date
     */
    @NotNull(message = "To date is required")
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate tDate;

    /**
     * Sale Date
     */
    @NotNull(message = "Sale date is required")
    @JsonFormat(pattern = "yyyy/MM/dd")
    private LocalDate saleDate;

    /**
     * Planning number display (e.g., "PL000000001")
     */
    private String cNumberDisplay;

    /**
     * Planning number (numeric part)
     */
    private Integer cNumber;

    /**
     * Remarks
     */
    @Size(max = 2000)
    private String remarks;

    /**
     * Last employee ref ID
     */
    private Integer lastEmployeeRefId;

    /**
     * Search text (comma-separated port codes)
     */
    private String search;

    /**
     * Planning details (jobs)
     */
    @Valid
    private List<PlanningDetailRequest> saleDetails;

    /**
     * Inner DTO for planning detail.
     *
     * IMPORTANT: Only these fields are SAVED to PLANINGDetails table:
     * - saleOrderMasterRefId (REQUIRED)
     * - truckRefid
     * - remarks
     * - originD
     * - destinationD
     * - truckNameD
     * - driverNameD
     * - sortBy (REQUIRED)
     * - pickupDateD
     * - deliveryDateD
     * - pickuptimelist
     * - pickupQuantitylist
     * - deliveryQuantitylist
     * - delivertimelist
     *
     * All other fields are for DISPLAY ONLY (come from SaleOrderMaster via JOIN)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanningDetailRequest {

        // ===== FIELDS SAVED TO PLANINGDetails =====

        /**
         * Sale order master reference ID (REQUIRED)
         */
        @NotNull(message = "Sale order master reference ID is required")
        private Integer saleOrderMasterRefId;

        /**
         * Truck reference ID (optional - overrides SaleOrder truck)
         */
        private Integer truckRefid;

        /**
         * Remarks for this planning detail
         */
        private String remarks;

        /**
         * Origin display (from SaleOrder)
         */
        private String originD;

        /**
         * Destination display (from SaleOrder)
         */
        private String destinationD;

        /**
         * Truck name (denormalized from SaleOrder)
         */
        private String truckNameD;

        /**
         * Driver name (denormalized from SaleOrder)
         */
        private String driverNameD;

        /**
         * Sort order (REQUIRED)
         */
        @NotNull(message = "Sort order is required")
        private Integer sortBy;

        /**
         * Pickup date/time
         */
        @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
        private LocalDateTime pickupDateD;

        /**
         * Delivery date/time
         */
        @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
        private LocalDateTime deliveryDateD;

        /**
         * Pickup time list (comma-separated times)
         */
        private String pickuptimelist;

        /**
         * Pickup quantity list (comma-separated quantities)
         */
        private String pickupQuantitylist;

        /**
         * Delivery quantity list (comma-separated quantities)
         */
        private String deliveryQuantitylist;

        /**
         * Delivery time list (comma-separated times)
         */
        private String delivertimelist;

        // ===== DISPLAY ONLY FIELDS (from SaleOrderMaster JOIN - NOT saved) =====

        /**
         * SDId - Detail ID (auto-generated, not from request)
         */
        private Integer sdid;

        /**
         * Job number (from SaleOrderMaster.CNumberDisplay)
         */
        private String jobNo;

        /**
         * Job date (from SaleOrderMaster.SaleDate)
         */
        private String jobDate;

        /**
         * Job status name (from JobStatusMaster)
         */
        private String jobStatus;

        /**
         * Customer name (from Customer)
         */
        private String customerName;

        /**
         * Origin (from SaleOrderMaster.Origin)
         */
        private String origin;

        /**
         * Destination (from SaleOrderMaster.Destination)
         */
        private String destination;

        /**
         * Vessel name (from SaleOrderMaster)
         */
        private String vesselName;

        /**
         * Package (Quantity/TotalWeight from SaleOrderMaster)
         */
        private String pkg;

        /**
         * Employee name (from SaleOrderMaster)
         */
        private String employeeName;

        /**
         * Truck size (from SaleOrderMaster)
         */
        private String truckSize;

        /**
         * Pickup date string (from SaleOrderMaster)
         */
        private String sPickupDate;

        /**
         * Delivery date string (from SaleOrderMaster)
         */
        private String sDeliveryDate;

        /**
         * L.ETA - ETA from SaleOrderMaster.ETA
         */
        private String leta;

        /**
         * O.ETA - Original ETA from SaleOrderMaster.OETA
         */
        private String oeta;

        /**
         * Warehouse enter date (from SaleOrderMaster)
         */
        private String sWareHouseEnterDate;

        /**
         * Warehouse exit date (from SaleOrderMaster)
         */
        private String sWareHouseExitDate;

        /**
         * Warehouse address (from SaleOrderMaster)
         */
        private String wareHouseAddress;

        /**
         * Pickup address (from SaleOrderMaster)
         */
        private String pickupAddress;

        /**
         * Delivery address (from SaleOrderMaster)
         */
        private String deliveryAddress;

        /**
         * Source port (from SaleOrderMaster.SPort)
         */
        private String sPort;

        /**
         * Origin port (from SaleOrderMaster.OPort)
         */
        private String oPort;

        /**
         * AWB No (from SaleOrderMaster)
         */
        private String awbNo;

        /**
         * BL Copy (from SaleOrderMaster)
         */
        private String blCopy;
    }
}
