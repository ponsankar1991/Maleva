package my.maleva.api.module.paymentrecept.print;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Everything the printed receipt voucher shows, read once and frozen — the
 * port of the legacy {@code SelectReceiptReportData} row set (the Crystal
 * {@code CRReceipt2.rpt} data source) with the report's formula fields
 * (amount in words, description fallback) computed here so the template
 * only prints.
 *
 * <p>Plain JavaBeans (Lombok {@code @Data}) rather than records because the
 * report engine reads fields through {@code getX()} accessors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptPrintSnapshot {

    /** The six lines beside the logo and the centred heading, from configuration. */
    private List<String> headerLines;
    private String heading;

    /** Header. */
    private Integer receiptId;
    private String receiptNo;
    private LocalDate receiptDate;
    /** RefNumber — printed as CHEQUE NO. */
    private String chequeNo;

    /** Customer box. */
    private String customerName;
    /** Customer.Address1, printed as typed (may hold line breaks). */
    private String customerAddress;
    /** Customer.OPhone — the TEL line. */
    private String customerPhone;
    /** Customer.City — the contact person, as every legacy print used it. */
    private String attentionName;

    /** The single A/C CODE / A/C NAME / DESCRIPTION / AMOUNT row. */
    private String accountCode;
    private String accountName;
    /** Receipt.Remarks. */
    private String description;
    private BigDecimal amount;

    /** SymbolMaster.CName ("SINGAPORE", "MALAYSIA"): the amount-in-words prefix, as Crystal printed it. */
    private String currencyName;
    /** SymbolMaster.SName ("SGD", "RM"). */
    private String currencySymbol;

    /** Totals box, sen-exact. */
    private BigDecimal subTotal;
    private BigDecimal roundingAdjustment;
    private BigDecimal netTotal;
    private String amountInWords;

    /** Static text from configuration. */
    private String nbNote;
    private String generatedNote;

    private List<ReceiptPrintLine> lines;

    /** One row of the Payment Details table. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiptPrintLine {
        private int rowNumber;
        /** INV for an invoice line, OB for the customer opening balance. */
        private String docType;
        private String docNo;
        private String docDate;
        /** Crystal {@code Description}: loading vessel, else off vessel. */
        private String description;
        private BigDecimal originalAmount;
        private BigDecimal paidAmount;
    }
}
