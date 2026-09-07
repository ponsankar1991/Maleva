package my.maleva.api.module.salecreditmaster.print;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Everything the printed credit note shows, read once and frozen — the port of
 * the legacy {@code SaleCreditServices.Printfunction} row set plus the Crystal
 * {@code CRCreditNote.rpt} formulas, computed here so the template only prints.
 *
 * <p>The field set follows the Crystal design box for box: the customer block
 * carries the phone and the attention name, the right-hand box's fourth row is
 * SALES MAN (not payment terms — that row does not exist on this document),
 * and the "Tax Invoice No" line is the invoice number with its date in
 * brackets. The amount in words is prefixed with the currency's <b>word</b>
 * ({@code SymbolMaster.CName}, e.g. SINGAPORE), which is what the legacy query
 * selected as {@code SymbolName}.
 *
 * <p>Plain JavaBeans rather than records: the report engine reads fields
 * through {@code getX()} accessors, and a record's {@code x()} is invisible
 * to it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditNotePrintSnapshot {

    /** The six lines beside the logo and the centred heading, from configuration. */
    private List<String> headerLines;
    private String heading;

    /** Header. */
    private Integer creditNoteId;
    private String creditNoteNo;
    private LocalDate creditNoteDate;

    /** The customer block: name, address, TEL and Attn. */
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    /** Customer.City — the contact person, as the entry screen uses it. */
    private String attentionName;

    /** The SALES MAN row: the employee who raised the note. */
    private String salesMan;

    /** The invoice this note credits, and the "Tax Invoice No" line built from it. */
    private String invoiceNo;
    private LocalDate invoiceDate;
    /** Crystal: {@code InvoiceNo + "(" + SInvoiceDate + ")"}; blank when unlinked. */
    private String taxInvoiceNo;

    /** Money, sen-exact. */
    private BigDecimal subtotal;
    private BigDecimal gstAmount;
    private BigDecimal roundingAdjustment;
    private BigDecimal netTotal;
    /** SymbolMaster.CName + the amount spelled out, e.g. "SINGAPORE  FOURTEEN AND TWENTY CENTS ONLY". */
    private String amountInWords;

    /** LHDN e-invoice; the whole block is hidden until the note has been submitted. */
    private String eInvoiceUid;
    private String eInvoiceLongId;
    private String eInvoiceStatus;
    private LocalDateTime eInvoiceValidatedAt;
    private String eInvoiceShareUrl;
    /** PNG bytes of the validation QR; null until LHDN has validated. */
    private byte[] qrPng;

    /** The numbered notes and the two small-print lines, from configuration. */
    private List<String> notes;
    private String generatedNoteLine1;
    private String generatedNoteLine2;

    private List<CreditNotePrintLine> lines;

    public boolean isEInvoiced() {
        return eInvoiceUid != null && !eInvoiceUid.isBlank();
    }

    /** One printed line — the ten columns of the Crystal detail band. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditNotePrintLine {
        private int rowNumber;
        private String productCode;
        private String description;
        private BigDecimal quantity;
        private String uom;
        private BigDecimal unitPrice;
        private BigDecimal discountAmount;
        /**
         * The value GST is charged on — the line's tax-exclusive amount when it
         * is taxed, and zero when it is not, which is what the sample document
         * prints for a 0% line.
         */
        private BigDecimal taxableAmount;
        private BigDecimal taxPercent;
        /** The GST in money — the AMT. column. */
        private BigDecimal taxAmount;
        /** The stored tax-inclusive line amount — the NET AMT. column. */
        private BigDecimal netAmount;
    }
}
