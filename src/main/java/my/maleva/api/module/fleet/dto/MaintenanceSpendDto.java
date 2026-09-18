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
    /**
     * Money out: purchase orders plus the payment vouchers that settle no order.
     *
     * Not the sum of the category figures above. Fuel, job orders, toll,
     * AutoPass and levi are records of what was bought, and that buying leaves
     * the company through an order or a voucher - adding both sides would count
     * the same money twice. See {@code recordedCostTotal} for what those
     * categories come to.
     */
    private BigDecimal grandTotal;

    /**
     * What the expense tables add up to: job orders + purchases + fuel +
     * AutoPass + toll + levi.
     *
     * A breakdown of what the money went on, not a second spend figure. It will
     * not equal {@code grandTotal}, and the gap is worth reading: cost recorded
     * against no order or voucher in the period, or orders covering things the
     * expense tables do not track.
     */
    private BigDecimal recordedCostTotal;

    /**
     * Everything in the range that belongs to no truck, as one extra row.
     *
     * The truck-wise queries all INNER JOIN TruckMaster, while the headline
     * totals do not, so a purchase order, job order, fuel entry or toll entry
     * with no usable TruckRefid lands in the total and in no row of the table.
     * Each figure here is that difference - company total minus what the trucks
     * account for - which is what lets the table add up to {@code grandTotal}
     * instead of stopping short of it. Purchase orders are the big one: most of
     * them are office, workshop and general buying, named to no vehicle.
     */
    private TruckSpend unassignedSpend;

    /**
     * Fuel subsidy received in the range (SubcdiyEntry).
     *
     * Reported beside the fuel figure, never subtracted from it:
     * {@code fuelTotal} is receipt value and has to keep tying to the Fuel
     * Entry screen. The subsidy is what comes back, shown so the real cost of
     * fuel is visible without changing what "spend" means.
     */
    private BigDecimal fuelSubsidyTotal;

    /** Payment vouchers raised for fuel in the range (Description = 'FUEL'). */
    private BigDecimal fuelPaymentVoucherTotal;

    /** How many RTI orders were delivered in the range (count only). */
    private Long rtiOrderCount;

    /** Per-truck spend and earnings across all sources. */
    private List<TruckSpend> truckSpend;

    /** Job order spend grouped by JobOrderTypeMaster.JobTypeName. */
    private List<NamedSpend> jobTypeSpend;

    /** Bill order spend grouped by BillsOrderMaster.Description. */
    private List<NamedSpend> billDescriptionSpend;

    /**
     * What actually left the bank in the range, and how it relates to the
     * purchase orders above.
     *
     * Kept apart from {@code grandTotal} on purpose. A bill and a payment
     * voucher both carry a BillsOrderMasterRefId, so each *is* the settlement
     * of a purchase order already counted in {@code billOrderTotal}. Folding
     * them into the spend total would count that money a second time.
     * Committed and released are the same money at two stages, so they are
     * reported side by side rather than added.
     */
    private PaymentRelease paymentRelease;

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
        /** Purchase orders naming this truck (BillsOrderMaster.TruckRefid). */
        private BigDecimal purchaseAmount;
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

    /**
     * The payment side: what was committed, and what has been released against it.
     *
     * Committed is the purchase order (BillsOrderMaster). Released is the two
     * documents that settle it - the bill (BillMaster) and the payment voucher
     * (PaymentVoucherMaster) - both of which carry a BillsOrderMasterRefId back
     * to the order they belong to.
     *
     * The bill and voucher totals are deliberately NOT summed into one released
     * figure. An order can be pushed to a bill and also have a voucher raised
     * against it, so adding the two would count that money twice; they are
     * reported as two lines under one heading instead.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentRelease {

        /** Committed: purchase orders raised in the range. Mirrors billOrderTotal. */
        private BigDecimal purchaseOrderTotal;
        private Long purchaseOrderCount;

        /** Released as a bill. */
        private BigDecimal billTotal;
        private Long billCount;

        /** Released as a payment voucher. */
        private BigDecimal voucherTotal;
        private Long voucherCount;

        /** Vouchers settling no purchase order - the part that counts as money out. */
        private BigDecimal standaloneVoucherTotal;
        private Long standaloneVoucherCount;

        /** Vouchers paying an order already counted on the committed side. */
        private BigDecimal voucherAgainstOrderTotal;

        /**
         * Purchase orders from the range that have a bill or a voucher against
         * them, and those that have neither.
         *
         * The settling document is looked for without a date bound: a March
         * order paid in April is settled, and calling it outstanding because
         * the payment fell outside the window would be wrong.
         */
        private BigDecimal settledTotal;
        private Long settledCount;
        private BigDecimal outstandingTotal;
        private Long outstandingCount;

        /** Voucher spend grouped by PaymentVoucherMaster.Description. */
        private List<NamedSpend> voucherByDescription;
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
