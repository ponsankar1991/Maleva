package my.maleva.api.module.invoice.einvoice;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Everything the e-invoice needs from the database, read once and frozen.
 *
 * <p>The builder and the validator work on this snapshot, never on JPA
 * entities: money is already {@link BigDecimal} at sen precision, the
 * currency is already normalised, and each line already carries the item and
 * UOM text it was joined to. That keeps the two classes that decide what the
 * government sees free of persistence concerns, and makes them unit-testable
 * with plain objects.
 *
 * @param header       the invoice row
 * @param customer     the buyer
 * @param lines        detail rows in {@code SaleDetails.Id} order, numbered from 1
 * @param loadProblems things the loader could not resolve (a missing UOM row,
 *                     a line whose item no longer exists). The validator turns
 *                     these into refusals; they are collected rather than thrown
 *                     so the operator sees all of them at once.
 */
@Builder(toBuilder = true)
public record EInvoiceSnapshot(
        Header header,
        Customer customer,
        List<Line> lines,
        List<EInvoiceProblem> loadProblems) {

    /** The invoice header. Money is sen-exact; see {@link EInvoiceMoney}. */
    @Builder(toBuilder = true)
    public record Header(
            Integer invoiceId,
            Integer companyId,
            /** CNumberDisplay, e.g. INV000000123 — LHDN's codeNumber. */
            String invoiceNo,
            LocalDateTime saleDate,
            /** Remarks1 — sent as the billing reference when present. */
            String referenceNo,
            /** SaleMaster.Amount — the tax-INCLUSIVE total the customer pays. */
            BigDecimal amount,
            /** SaleMaster.TaxAmount — total tax across lines. */
            BigDecimal taxAmount,
            /** SaleMaster.GrossAmount — the screen stores this equal to Amount. */
            BigDecimal grossAmount,
            boolean active,
            String eInvoiceUid,
            String eInvoiceSubmissionUid,
            String eInvoiceLongId,
            String eInvoiceStatus) {

        public boolean alreadySubmitted() {
            return eInvoiceUid != null && !eInvoiceUid.isBlank();
        }
    }

    /** The buyer party as LHDN will see it. */
    @Builder(toBuilder = true)
    public record Customer(
            Integer customerId,
            String name,
            String tin,
            String registrationNo,
            String phone,
            String email,
            /** Customer.CustomerCity — the real city (Customer.City is the contact person). */
            String city,
            String postalZone,
            String address1,
            String state,
            /** ISO 3166-1 alpha-3 from CountryMaster, or empty when the customer has no country. */
            String countryCode,
            /** ISO 4217 after normalisation (RM → MYR, trimmed, upper-cased). */
            String currencyCode) {
    }

    /** One detail row. Stored amounts are as the screen saved them: {@code amount} is tax-inclusive. */
    @Builder(toBuilder = true)
    public record Line(
            /** 1-based position, for messages an operator can match to the screen. */
            int rowNumber,
            Integer detailId,
            Integer itemMasterRefId,
            String productCode,
            String productName,
            /** SDRemarks — preferred over the product name as the line description. */
            String remarks,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal taxPercent,
            BigDecimal taxAmount,
            /** SaleDetails.Amount — Qty × Rate + tax, as stored. */
            BigDecimal amount,
            /** UOM.Description text, e.g. UNIT(S); null when the item's UOM row is missing. */
            String uom,
            /** ItemMaster.SaleClassification → Classification.ClassificationCode; null when unset. */
            Integer classificationCode) {

        public String label() {
            return "line " + rowNumber + (productCode == null || productCode.isBlank() ? "" : " (" + productCode + ")");
        }

        public boolean isTaxed() {
            return taxPercent != null && taxPercent.signum() != 0;
        }
    }
}
