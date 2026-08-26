package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * MaintenanceSpendDto - spending analytics for the fleet maintenance dashboard.
 *
 * Aggregates, for one company and date range, everything the workshop spends:
 * job orders (truck-wise and job-type-wise), bill orders (description-wise),
 * and the running pass costs (AutoPass, Toll, Levi).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceSpendDto {

    private LocalDate fromDate;
    private LocalDate toDate;

    private BigDecimal jobOrderTotal;
    private BigDecimal billOrderTotal;
    private BigDecimal fuelTotal;
    private BigDecimal fuelLiters;
    /** How many fuel entries were recorded in the range. */
    private Long fuelEntryCount;
    private BigDecimal autoPassTotal;
    private BigDecimal tollTotal;
    private BigDecimal leviTotal;
    private BigDecimal grandTotal;

    /** How many RTI orders were delivered in the range (count only). */
    private Long rtiOrderCount;

    /** Per-truck spend and earnings across all sources. */
    private List<TruckSpend> truckSpend;

    /** Job order spend grouped by JobOrderTypeMaster.JobTypeName. */
    private List<NamedSpend> jobTypeSpend;

    /** Bill order spend grouped by BillsOrderMaster.Description. */
    private List<NamedSpend> billDescriptionSpend;

    /** Day-by-day totals across the range, oldest first. */
    private List<DailySpend> dailySpend;

    /** Individual purchase orders (BillsOrderMaster rows) in the range, newest first, capped. */
    private List<PurchaseOrderDetail> purchaseOrderDetails;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TruckSpend {
        private Integer truckId;
        private String truckName;
        private BigDecimal jobOrderAmount;
        private BigDecimal fuelAmount;
        private BigDecimal fuelLiters;
        private Long fuelEntryCount;
        private BigDecimal autoPassAmount;
        private BigDecimal tollAmount;
        private BigDecimal leviAmount;
        private BigDecimal totalAmount;
        private Long rtiOrderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailySpend {
        private LocalDate date;
        private BigDecimal jobOrderAmount;
        private BigDecimal purchaseAmount;
        private BigDecimal fuelAmount;
        /** AutoPass + Toll + Levi together. */
        private BigDecimal passAmount;
        private BigDecimal totalSpend;
        /** RTI orders delivered that day (count only). */
        private Long rtiOrderCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NamedSpend {
        private String name;
        private Long entryCount;
        private BigDecimal totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PurchaseOrderDetail {
        private LocalDate date;
        /** BillsOrderMaster.CNumberDisplay — the PO number. */
        private String poNumber;
        private String description;
        private String supplierName;
        private String payTo;
        private String invoiceNo;
        private String truckName;
        private BigDecimal amount;
    }
}
