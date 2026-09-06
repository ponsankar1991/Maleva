package my.maleva.api.module.invoice.einvoice;

import my.maleva.api.common.config.MyInvoisProperties;
import my.maleva.api.integration.myinvois.ubl.UblDocument;
import my.maleva.api.integration.myinvois.ubl.UblInvoice;
import my.maleva.api.integration.myinvois.ubl.UblValues;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static my.maleva.api.integration.myinvois.ubl.UblValues.amount;
import static my.maleva.api.integration.myinvois.ubl.UblValues.classification;
import static my.maleva.api.integration.myinvois.ubl.UblValues.country;
import static my.maleva.api.integration.myinvois.ubl.UblValues.currency;
import static my.maleva.api.integration.myinvois.ubl.UblValues.id;
import static my.maleva.api.integration.myinvois.ubl.UblValues.industry;
import static my.maleva.api.integration.myinvois.ubl.UblValues.numeric;
import static my.maleva.api.integration.myinvois.ubl.UblValues.quantity;
import static my.maleva.api.integration.myinvois.ubl.UblValues.text;
import static my.maleva.api.integration.myinvois.ubl.UblValues.typeCode;

/**
 * Turns a validated {@link EInvoiceSnapshot} into the UBL document LHDN receives.
 *
 * <p>Pure: no I/O, no clock of its own, no repository. Given the same
 * snapshot, supplier and instant it produces the same document, which is what
 * makes it testable against a golden JSON.
 *
 * <p>Element-by-element this matches what the legacy system sent and LHDN
 * accepted — same codes, same constants, same optional-element rules — with
 * these deliberate differences, each explained where it happens:
 * money is sen-exact (legacy sent float32 noise); line amounts follow
 * {@link MyInvoisProperties.LineAmountPolicy}; line IDs are 1-based;
 * the taxed subtotal carries its taxable amount and rate.
 */
@Component
public class EInvoiceDocumentBuilder {

    /** LHDN document type 01 = Invoice; list version 1.0. */
    static final String INVOICE_TYPE = "01";
    static final String INVOICE_TYPE_VERSION = "1.0";

    /** UN/ECE Recommendation 20 code for "unit" — what legacy sent for every InvoicedQuantity. */
    static final String UNIT_CODE_C62 = "C62";

    /** LHDN tax types: 01 = sales tax (SST), 06 = not applicable. */
    static final String TAX_TYPE_SALES = "01";
    static final String TAX_TYPE_NOT_APPLICABLE = "06";

    /** LHDN's address line limit; the customer master allows 300 characters. */
    static final int ADDRESS_LINE_MAX = 150;

    private final MyInvoisProperties properties;

    public EInvoiceDocumentBuilder(MyInvoisProperties properties) {
        this.properties = properties;
    }

    /**
     * @param snapshot a snapshot that passed {@link EInvoiceValidator}
     * @param issuedAt the moment of submission; LHDN requires the issue
     *                 date/time to be within its submission window, so — as
     *                 legacy did — the push instant is used, not the sale date
     */
    public UblDocument build(EInvoiceSnapshot snapshot, Instant issuedAt) {
        EInvoiceSnapshot.Header h = snapshot.header();
        EInvoiceSnapshot.Customer c = snapshot.customer();
        String ccy = c.currencyCode();

        List<UblInvoice.InvoiceLine> lines = buildLines(snapshot.lines(), ccy);
        Totals totals = Totals.of(snapshot);

        UblInvoice.Invoice invoice = UblInvoice.Invoice.builder()
                .id(id(h.invoiceNo()))
                .issueDate(UblValues.utcDate(issuedAt))
                .issueTime(UblValues.utcTime(issuedAt))
                .invoiceTypeCode(typeCode(INVOICE_TYPE, INVOICE_TYPE_VERSION))
                .documentCurrencyCode(currency(ccy))
                .taxCurrencyCode(currency(ccy))
                .invoicePeriod(List.of(new UblInvoice.InvoicePeriod(null, null, text("Monthly"))))
                .billingReference(billingReference(h.referenceNo()))
                .accountingSupplierParty(List.of(new UblInvoice.SupplierParty(List.of(supplierParty()))))
                .accountingCustomerParty(List.of(new UblInvoice.CustomerParty(List.of(customerParty(c)))))
                .taxTotal(List.of(documentTaxTotal(snapshot, totals, ccy)))
                .legalMonetaryTotal(List.of(UblInvoice.LegalMonetaryTotal.builder()
                        .lineExtensionAmount(amount(totals.exclusive, ccy))
                        .taxExclusiveAmount(amount(totals.exclusive, ccy))
                        .taxInclusiveAmount(amount(totals.inclusive, ccy))
                        .allowanceTotalAmount(amount(EInvoiceMoney.zero(), ccy))
                        .chargeTotalAmount(amount(EInvoiceMoney.zero(), ccy))
                        .payableRoundingAmount(amount(EInvoiceMoney.zero(), ccy))
                        .payableAmount(amount(totals.inclusive, ccy))
                        .build()))
                .invoiceLine(lines)
                .build();

        return UblDocument.of(invoice);
    }

    // ────────────────────────────────────────────────────────────── parties ──

    /** Maleva, from configuration. The identifications are sent in TIN, BRN, SST order. */
    UblInvoice.Party supplierParty() {
        MyInvoisProperties.Supplier s = properties.supplier();
        return UblInvoice.Party.builder()
                .industryClassificationCode(industry(s.getMsicCode(), s.getMsicDescription()))
                .partyIdentification(identifications(
                        UblInvoice.PartyIdentification.of(s.getTin(), s.getTinScheme()),
                        UblInvoice.PartyIdentification.of(s.getRegistrationNo(), s.getRegistrationScheme()),
                        UblInvoice.PartyIdentification.of(s.getSstNo(), s.getSstScheme())))
                .postalAddress(List.of(UblInvoice.PostalAddress.builder()
                        .cityName(text(s.getCity()))
                        .postalZone(text(s.getPostalZone()))
                        .countrySubentityCode(text(s.getStateCode()))
                        .addressLine(addressLines(s.getAddressLine1(), s.getAddressLine2()))
                        .country(List.of(new UblInvoice.Country(country(s.getCountryCode()))))
                        .build()))
                .partyLegalEntity(List.of(new UblInvoice.PartyLegalEntity(text(s.getName()))))
                .contact(List.of(new UblInvoice.Contact(text(s.getPhone()), text(s.getEmail()))))
                .build();
    }

    /**
     * The buyer. Identifications are TIN then BRN, each omitted when blank
     * (the validator has already refused a blank TIN). The registration number
     * is always labelled BRN, as legacy did — the customer master does not say
     * whether an individual's NRIC was typed there instead.
     */
    UblInvoice.Party customerParty(EInvoiceSnapshot.Customer c) {
        return UblInvoice.Party.builder()
                .partyIdentification(identifications(
                        UblInvoice.PartyIdentification.of(trim(c.tin()), "TIN"),
                        UblInvoice.PartyIdentification.of(trim(c.registrationNo()), "BRN")))
                .postalAddress(List.of(UblInvoice.PostalAddress.builder()
                        .cityName(text(c.city()))
                        .postalZone(text(c.postalZone()))
                        .countrySubentityCode(text(MalaysianStateCodes.subentityCode(c.countryCode(), c.state()).orElse(null)))
                        .addressLine(chunkedAddressLines(c.address1()))
                        .country(List.of(new UblInvoice.Country(country(c.countryCode()))))
                        .build()))
                .partyLegalEntity(List.of(new UblInvoice.PartyLegalEntity(text(c.name()))))
                // Legacy never sent the customer's email; kept that way.
                .contact(List.of(new UblInvoice.Contact(text(c.phone()), null)))
                .build();
    }

    private static List<UblInvoice.PartyIdentification> identifications(UblInvoice.PartyIdentification... candidates) {
        List<UblInvoice.PartyIdentification> present = new ArrayList<>();
        for (UblInvoice.PartyIdentification candidate : candidates) {
            if (candidate != null) {
                present.add(candidate);
            }
        }
        return present.isEmpty() ? null : present;
    }

    /** Fixed lines (the supplier's two configured lines), blanks skipped. */
    private static List<UblInvoice.AddressLine> addressLines(String... lines) {
        List<UblInvoice.AddressLine> result = new ArrayList<>();
        for (String line : lines) {
            if (line != null && !line.isBlank()) {
                result.add(new UblInvoice.AddressLine(text(line)));
            }
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * A free-text address cut into 150-character pieces, as legacy did.
     * Cut on raw characters rather than words to keep the exact text LHDN
     * already holds for these customers; whitespace-only pieces are dropped.
     */
    static List<UblInvoice.AddressLine> chunkedAddressLines(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        List<UblInvoice.AddressLine> result = new ArrayList<>();
        for (int start = 0; start < address.length(); start += ADDRESS_LINE_MAX) {
            String piece = address.substring(start, Math.min(address.length(), start + ADDRESS_LINE_MAX));
            if (!piece.isBlank()) {
                result.add(new UblInvoice.AddressLine(text(piece)));
            }
        }
        return result.isEmpty() ? null : result;
    }

    private static List<UblInvoice.BillingReference> billingReference(String referenceNo) {
        if (referenceNo == null || referenceNo.isBlank()) {
            return null;
        }
        return List.of(new UblInvoice.BillingReference(
                List.of(new UblInvoice.DocumentReference(id(referenceNo)))));
    }

    // ──────────────────────────────────────────────────────────────── lines ──

    List<UblInvoice.InvoiceLine> buildLines(List<EInvoiceSnapshot.Line> lines, String ccy) {
        List<UblInvoice.InvoiceLine> result = new ArrayList<>(lines.size());
        for (EInvoiceSnapshot.Line line : lines) {
            BigDecimal lineAmount = lineAmount(line);
            String taxType = line.isTaxed() ? TAX_TYPE_SALES : TAX_TYPE_NOT_APPLICABLE;

            result.add(UblInvoice.InvoiceLine.builder()
                    // Legacy numbered lines from 0; LHDN's samples start at 1.
                    .id(id(String.valueOf(line.rowNumber())))
                    .invoicedQuantity(quantity(line.quantity(), UNIT_CODE_C62))
                    .lineExtensionAmount(amount(lineAmount, ccy))
                    .taxTotal(List.of(new UblInvoice.TaxTotal(
                            amount(line.taxAmount(), ccy),
                            List.of(UblInvoice.TaxSubtotal.builder()
                                    .taxableAmount(amount(lineAmount, ccy))
                                    .taxAmount(amount(line.taxAmount(), ccy))
                                    .percent(numeric(line.taxPercent()))
                                    .taxCategory(List.of(new UblInvoice.TaxCategory(
                                            id(taxType), null, UblInvoice.TaxScheme.other())))
                                    // Fixed-rate fields legacy always sent; LHDN accepts them on a percentage tax.
                                    .perUnitAmount(amount(line.unitPrice(), ccy))
                                    .baseUnitMeasure(quantity(line.quantity(), line.uom()))
                                    .build()))))
                    .item(List.of(new UblInvoice.Item(
                            List.of(new UblInvoice.CommodityClassification(
                                    classification(classificationCode(line.classificationCode())))),
                            text(line.remarks() == null || line.remarks().isBlank() ? line.productName() : line.remarks()))))
                    .price(List.of(new UblInvoice.Price(amount(line.unitPrice(), ccy))))
                    .itemPriceExtension(List.of(new UblInvoice.ItemPriceExtension(amount(lineAmount, ccy))))
                    .build());
        }
        return result;
    }

    /**
     * What a line reports as its amount. The stored figure is tax-inclusive;
     * UBL's line amounts are tax-exclusive. See
     * {@link MyInvoisProperties#getLineAmountPolicy()} for why both exist.
     */
    BigDecimal lineAmount(EInvoiceSnapshot.Line line) {
        return switch (properties.getLineAmountPolicy()) {
            case EXCLUSIVE -> EInvoiceMoney.round(line.amount().subtract(line.taxAmount()));
            case LEGACY_INCLUSIVE -> line.amount();
        };
    }

    /** LHDN classification codes are three digits: 22 → "022". Zero is "missing", never "000". */
    static String classificationCode(Integer code) {
        return code == null || code <= 0 ? null : String.format("%03d", code);
    }

    // ───────────────────────────────────────────────────────────────── tax ──

    /**
     * The document-level tax. One subtotal per tax type present: 01 carrying
     * the whole invoice tax, and 06 with zero for the untaxed lines.
     *
     * <p>Under {@code EXCLUSIVE} each subtotal also states its taxable base
     * (and the rate on 01), which legacy never sent; the two bases add up to
     * the header's tax-exclusive total by construction. Under
     * {@code LEGACY_INCLUSIVE} they are omitted, so the document is exactly
     * the shape the old system produced.
     */
    UblInvoice.TaxTotal documentTaxTotal(EInvoiceSnapshot snapshot, Totals totals, String ccy) {
        boolean legacyShape = properties.getLineAmountPolicy() == MyInvoisProperties.LineAmountPolicy.LEGACY_INCLUSIVE;
        List<UblInvoice.TaxSubtotal> subtotals = new ArrayList<>(2);

        if (totals.taxedRate != null) {
            subtotals.add(UblInvoice.TaxSubtotal.builder()
                    .taxableAmount(legacyShape ? null : amount(totals.taxedExclusive, ccy))
                    .taxAmount(amount(snapshot.header().taxAmount(), ccy))
                    .percent(legacyShape ? null : numeric(totals.taxedRate))
                    .taxCategory(List.of(new UblInvoice.TaxCategory(
                            id(TAX_TYPE_SALES), null, UblInvoice.TaxScheme.other())))
                    .build());
        }
        if (totals.hasUntaxedLines) {
            subtotals.add(UblInvoice.TaxSubtotal.builder()
                    .taxableAmount(legacyShape ? null : amount(totals.untaxedExclusive, ccy))
                    .taxAmount(amount(EInvoiceMoney.zero(), ccy))
                    .taxCategory(List.of(new UblInvoice.TaxCategory(
                            id(TAX_TYPE_NOT_APPLICABLE), null, UblInvoice.TaxScheme.other())))
                    .build());
        }
        return new UblInvoice.TaxTotal(amount(snapshot.header().taxAmount(), ccy), subtotals);
    }

    /**
     * Header figures derived from the (validated) snapshot.
     *
     * <p>The untaxed base is the exact sum of the untaxed lines (their tax is
     * zero, so nothing was rounded). The taxed base is the header's exclusive
     * total minus that, NOT a sum of the taxed lines: the header tax is a sum
     * of unrounded line taxes, so a sum of individually rounded lines can miss
     * it by a sen, and the two subtotal bases must add up to the header.
     */
    record Totals(BigDecimal inclusive, BigDecimal exclusive, BigDecimal taxedExclusive,
                  BigDecimal untaxedExclusive, BigDecimal taxedRate, boolean hasUntaxedLines) {

        static Totals of(EInvoiceSnapshot snapshot) {
            EInvoiceSnapshot.Header h = snapshot.header();
            BigDecimal inclusive = h.amount();
            // Computed in BigDecimal from the two rounded header figures, not
            // taken from SQL "Amount - TaxAmount" evaluated in float32.
            BigDecimal exclusive = EInvoiceMoney.round(h.amount().subtract(h.taxAmount()));

            BigDecimal untaxedExclusive = EInvoiceMoney.zero();
            BigDecimal taxedRate = null;
            boolean hasUntaxed = false;
            for (EInvoiceSnapshot.Line line : snapshot.lines()) {
                if (line.isTaxed()) {
                    taxedRate = line.taxPercent();
                } else {
                    untaxedExclusive = untaxedExclusive.add(line.amount());
                    hasUntaxed = true;
                }
            }
            BigDecimal taxedExclusive = taxedRate == null ? EInvoiceMoney.zero() : exclusive.subtract(untaxedExclusive);
            return new Totals(inclusive, exclusive, taxedExclusive, untaxedExclusive, taxedRate, hasUntaxed);
        }
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
