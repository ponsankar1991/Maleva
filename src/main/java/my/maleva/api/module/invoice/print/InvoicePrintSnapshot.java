package my.maleva.api.module.invoice.print;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Everything the printed invoice shows, read once and frozen — the port of
 * the legacy {@code Printfunction} row set plus the Crystal formula fields
 * ({@code CN}, {@code BLAWB}, {@code Land}, {@code off}, {@code PName},
 * {@code subTotalAmount}, {@code Amountinwords}), computed here so the
 * template only prints.
 *
 * <p>Plain JavaBeans (Lombok {@code @Data}) rather than records because the
 * report engine reads fields through {@code getX()} accessors; a record's
 * {@code x()} is invisible to it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePrintSnapshot {

    /** The six lines beside the logo and the centred heading, from configuration. */
    private List<String> headerLines;
    private String heading;

    /** Header. */
    private Integer invoiceId;
    private String invoiceNo;
    private LocalDate invoiceDate;
    /** Crystal {@code CN}: customer name, or {@code name/remarks} when the invoice has remarks. */
    private String customerLine;
    private String customerAddress;
    /** Customer.City — the contact person, as the legacy screen used it. */
    private String attentionName;
    private String attentionPhone;
    private String paymentTerms;
    /** SymbolMaster.SName, defaulting to RM as legacy did. */
    private String currencySymbol;

    /**
     * Job block. Blank on an invoice that covers more than one sale order —
     * legacy's {@code countsf > 1} rule, kept: there is no single job to name.
     */
    private String jobNo;
    private String origin;
    private String destination;
    private String weight;
    private String packages;
    /** Crystal {@code Land}: the loading vessel, or NIL. */
    private String vesselOnboard;
    /** Crystal {@code off}: the off vessel, or NIL. */
    private String vesselOffland;
    private String doNo;
    private String commodity;
    private LocalDate collectionDate;
    private LocalDate deliveryDate;
    /** Crystal {@code BLAWB}: AWB and BL joined with a slash when both exist. */
    private String blAwb;
    private String truckSize;
    private String truckName;
    /** Remarks1 — the "Reference" row. */
    private String reference;

    /** Money, sen-exact. */
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal roundingAdjustment;
    private BigDecimal netTotal;
    private String amountInWords;

    /** LHDN e-invoice. */
    private String eInvoiceUid;
    private String eInvoiceLongId;
    private String eInvoiceStatus;
    private LocalDateTime eInvoiceValidatedAt;
    private String eInvoiceShareUrl;
    /** PNG bytes of the validation QR; null until LHDN has validated. */
    private byte[] qrPng;

    /** The numbered notes and the small print, from configuration. */
    private List<String> notes;
    private String generatedNote;

    private List<InvoicePrintLine> lines;

    public boolean isEInvoiced() {
        return eInvoiceUid != null && !eInvoiceUid.isBlank();
    }

    /** One printed line. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoicePrintLine {
        private int rowNumber;
        private String productCode;
        /** Crystal {@code PName}: SDRemarks when typed, else the item name. */
        private String description;
        private BigDecimal quantity;
        private String uom;
        private BigDecimal unitPrice;
        private BigDecimal discountPercent;
        private String taxCode;
        private BigDecimal taxPercent;
        private BigDecimal taxAmount;
        /** Crystal {@code subTotalAmount}: unit price × quantity. */
        private BigDecimal lineSubtotal;
        /** The stored tax-inclusive line amount — the NET AMOUNT column. */
        private BigDecimal amount;
    }
}
