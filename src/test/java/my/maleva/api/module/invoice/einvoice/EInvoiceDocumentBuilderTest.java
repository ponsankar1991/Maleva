package my.maleva.api.module.invoice.einvoice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.common.config.MyInvoisProperties;
import my.maleva.api.integration.myinvois.MyInvoisDocumentCodec;
import my.maleva.api.integration.myinvois.ubl.UblDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the wire document. Assertions read the serialised JSON, not the Java
 * records, so a change to either the model or the codec shows up here.
 */
class EInvoiceDocumentBuilderTest {

    /** 2026-09-05 01:30:00 UTC = 09:30 in Malaysia; the document must carry the UTC values. */
    private static final Instant ISSUED_AT = Instant.parse("2026-09-05T01:30:00Z");

    private MyInvoisProperties properties;
    private EInvoiceDocumentBuilder builder;
    private final MyInvoisDocumentCodec codec = new MyInvoisDocumentCodec();
    private final ObjectMapper reader = new ObjectMapper();

    @BeforeEach
    void setUp() {
        properties = EInvoiceFixtures.properties();
        builder = new EInvoiceDocumentBuilder(properties);
    }

    @Test
    void rootCarriesTheThreeUblNamespacesAndOneInvoice() throws Exception {
        JsonNode root = json(builder.build(EInvoiceFixtures.snapshot(), ISSUED_AT));

        assertThat(root.get("_D").asText()).isEqualTo(UblDocument.NS_INVOICE);
        assertThat(root.get("_A").asText()).isEqualTo(UblDocument.NS_AGGREGATE);
        assertThat(root.get("_B").asText()).isEqualTo(UblDocument.NS_BASIC);
        assertThat(root.get("Invoice")).hasSize(1);
    }

    @Test
    void headerFieldsMatchLegacyConstantsAndUtcClock() throws Exception {
        JsonNode inv = invoice(EInvoiceFixtures.snapshot());

        assertThat(inv.at("/ID/0/_").asText()).isEqualTo("INV000004711");
        assertThat(inv.at("/IssueDate/0/_").asText()).isEqualTo("2026-09-05");
        assertThat(inv.at("/IssueTime/0/_").asText()).isEqualTo("01:30:00Z");
        assertThat(inv.at("/InvoiceTypeCode/0/_").asText()).isEqualTo("01");
        assertThat(inv.at("/InvoiceTypeCode/0/listVersionID").asText()).isEqualTo("1.0");
        assertThat(inv.at("/DocumentCurrencyCode/0/_").asText()).isEqualTo("MYR");
        assertThat(inv.at("/TaxCurrencyCode/0/_").asText()).isEqualTo("MYR");
        assertThat(inv.at("/InvoicePeriod/0/Description/0/_").asText()).isEqualTo("Monthly");
        assertThat(inv.at("/BillingReference/0/AdditionalDocumentReference/0/ID/0/_").asText()).isEqualTo("PO-778");
    }

    @Test
    void billingReferenceIsOmittedWhenThereIsNoReferenceNumber() throws Exception {
        EInvoiceSnapshot snapshot = EInvoiceFixtures.snapshot().toBuilder()
                .header(EInvoiceFixtures.header().toBuilder().referenceNo("  ").build()).build();
        assertThat(invoice(snapshot).has("BillingReference")).isFalse();
    }

    @Test
    void supplierPartyComesFromTheConfiguredProfile() throws Exception {
        JsonNode party = invoice(EInvoiceFixtures.snapshot()).at("/AccountingSupplierParty/0/Party/0");

        assertThat(party.at("/IndustryClassificationCode/0/_").asText()).isEqualTo("52299");
        assertThat(party.at("/IndustryClassificationCode/0/name").asText()).contains("transportation");
        assertThat(party.at("/PartyIdentification/0/ID/0/_").asText()).isEqualTo("C22173439020");
        assertThat(party.at("/PartyIdentification/0/ID/0/schemeID").asText()).isEqualTo("TIN");
        assertThat(party.at("/PartyIdentification/1/ID/0/schemeID").asText()).isEqualTo("BRN");
        assertThat(party.at("/PartyIdentification/2/ID/0/schemeID").asText()).isEqualTo("SST");
        assertThat(party.at("/PostalAddress/0/CountrySubentityCode/0/_").asText()).isEqualTo("10");
        assertThat(party.at("/PostalAddress/0/AddressLine")).hasSize(2);
        assertThat(party.at("/PostalAddress/0/Country/0/IdentificationCode/0/_").asText()).isEqualTo("MYS");
        assertThat(party.at("/PostalAddress/0/Country/0/IdentificationCode/0/listID").asText()).isEqualTo("ISO3166-1");
        assertThat(party.at("/Contact/0/ElectronicMail/0/_").asText()).isEqualTo("operation@maleva.com.my");
    }

    @Test
    void customerPartyUsesTinThenBrnAndOmitsEmail() throws Exception {
        JsonNode party = invoice(EInvoiceFixtures.snapshot()).at("/AccountingCustomerParty/0/Party/0");

        assertThat(party.has("IndustryClassificationCode")).isFalse();
        assertThat(party.at("/PartyIdentification/0/ID/0/_").asText()).isEqualTo("C1234567890");
        assertThat(party.at("/PartyIdentification/0/ID/0/schemeID").asText()).isEqualTo("TIN");
        assertThat(party.at("/PartyIdentification/1/ID/0/_").asText()).isEqualTo("201101015652");
        assertThat(party.at("/PartyIdentification/1/ID/0/schemeID").asText()).isEqualTo("BRN");
        assertThat(party.at("/PostalAddress/0/CityName/0/_").asText()).isEqualTo("SHAH ALAM");
        assertThat(party.at("/PostalAddress/0/PostalZone/0/_").asText()).isEqualTo("40000");
        assertThat(party.at("/PostalAddress/0/CountrySubentityCode/0/_").asText()).isEqualTo("10");
        assertThat(party.at("/PartyLegalEntity/0/RegistrationName/0/_").asText()).isEqualTo("ACME LOGISTICS SDN BHD");
        assertThat(party.at("/Contact/0/Telephone/0/_").asText()).isEqualTo("+60312345678");
        // Legacy never sent the customer's email; parity.
        assertThat(party.at("/Contact/0").has("ElectronicMail")).isFalse();
    }

    @Test
    void longAddressesAreCutInto150CharacterLines() {
        String address = "A".repeat(150) + "B".repeat(20);
        List<?> lines = EInvoiceDocumentBuilder.chunkedAddressLines(address);
        assertThat(lines).hasSize(2);

        assertThat(EInvoiceDocumentBuilder.chunkedAddressLines("short")).hasSize(1);
        assertThat(EInvoiceDocumentBuilder.chunkedAddressLines("   ")).isNull();
    }

    @Test
    void exclusivePolicySendsQtyTimesRateOnTheLine() throws Exception {
        properties.setLineAmountPolicy(MyInvoisProperties.LineAmountPolicy.EXCLUSIVE);
        JsonNode line = invoice(EInvoiceFixtures.snapshot()).at("/InvoiceLine/0");

        assertThat(line.at("/ID/0/_").asText()).isEqualTo("1");
        assertThat(line.at("/InvoicedQuantity/0/_").decimalValue()).isEqualByComparingTo("2.00");
        assertThat(line.at("/InvoicedQuantity/0/unitCode").asText()).isEqualTo("C62");
        assertThat(line.at("/LineExtensionAmount/0/_").decimalValue()).isEqualByComparingTo("200.00");
        assertThat(line.at("/TaxTotal/0/TaxAmount/0/_").decimalValue()).isEqualByComparingTo("12.00");
        assertThat(line.at("/TaxTotal/0/TaxSubtotal/0/TaxableAmount/0/_").decimalValue()).isEqualByComparingTo("200.00");
        assertThat(line.at("/TaxTotal/0/TaxSubtotal/0/Percent/0/_").decimalValue()).isEqualByComparingTo("6.00");
        assertThat(line.at("/TaxTotal/0/TaxSubtotal/0/TaxCategory/0/ID/0/_").asText()).isEqualTo("01");
        assertThat(line.at("/TaxTotal/0/TaxSubtotal/0/TaxCategory/0/TaxScheme/0/ID/0/_").asText()).isEqualTo("OTH");
        assertThat(line.at("/TaxTotal/0/TaxSubtotal/0/TaxCategory/0/TaxScheme/0/ID/0/schemeID").asText()).isEqualTo("UN/ECE 5153");
        assertThat(line.at("/TaxTotal/0/TaxSubtotal/0/BaseUnitMeasure/0/unitCode").asText()).isEqualTo("UNIT(S)");
        assertThat(line.at("/Item/0/CommodityClassification/0/ItemClassificationCode/0/_").asText()).isEqualTo("022");
        assertThat(line.at("/Item/0/CommodityClassification/0/ItemClassificationCode/0/listID").asText()).isEqualTo("CLASS");
        assertThat(line.at("/Item/0/Description/0/_").asText()).isEqualTo("HANDLING CHARGES VESSEL MV STAR");
        assertThat(line.at("/Price/0/PriceAmount/0/_").decimalValue()).isEqualByComparingTo("100.00");
        assertThat(line.at("/ItemPriceExtension/0/Amount/0/_").decimalValue()).isEqualByComparingTo("200.00");
    }

    @Test
    void legacyPolicySendsTheStoredInclusiveFigureAndTheOldDocumentShape() throws Exception {
        properties.setLineAmountPolicy(MyInvoisProperties.LineAmountPolicy.LEGACY_INCLUSIVE);
        JsonNode inv = invoice(EInvoiceFixtures.snapshot());
        JsonNode line = inv.at("/InvoiceLine/0");

        assertThat(line.at("/LineExtensionAmount/0/_").decimalValue()).isEqualByComparingTo("212.00");
        assertThat(line.at("/TaxTotal/0/TaxSubtotal/0/TaxableAmount/0/_").decimalValue()).isEqualByComparingTo("212.00");
        // legacy never sent a taxable base or rate on the document subtotals
        JsonNode taxed = inv.at("/TaxTotal/0/TaxSubtotal/0");
        assertThat(taxed.has("TaxableAmount")).isFalse();
        assertThat(taxed.has("Percent")).isFalse();
        assertThat(inv.at("/TaxTotal/0/TaxSubtotal/1").has("TaxableAmount")).isFalse();
        // and the header stays what legacy sent
        assertThat(inv.at("/LegalMonetaryTotal/0/LineExtensionAmount/0/_").decimalValue()).isEqualByComparingTo("250.00");
    }

    @Test
    void subtotalBasesCloseOnTheHeaderEvenWithHalfSenLineTaxes() throws Exception {
        // 2 × (3 × 33.33 @ 6%): tax 5.9994 each → stored 6.00 each, header tax 12.00 (sum of unrounded = 11.9988 → 12.00)
        // line inclusive 105.99 each → header 211.98; exclusive header 199.98; taxed base must be 199.98, not 2 × 99.99 = 199.98 here
        // but with a 0% line of 0.50 the taxed base is header-exclusive minus 0.50 by construction.
        EInvoiceSnapshot.Line t = EInvoiceFixtures.taxedLine().toBuilder()
                .quantity(EInvoiceFixtures.money("3.00")).unitPrice(EInvoiceFixtures.money("33.33"))
                .taxAmount(EInvoiceFixtures.money("6.00")).amount(EInvoiceFixtures.money("105.99")).build();
        EInvoiceSnapshot.Line u = EInvoiceFixtures.untaxedLine().toBuilder()
                .quantity(EInvoiceFixtures.money("1.00")).unitPrice(EInvoiceFixtures.money("0.50"))
                .taxAmount(EInvoiceFixtures.money("0.00")).amount(EInvoiceFixtures.money("0.50")).build();
        EInvoiceSnapshot snapshot = EInvoiceFixtures.snapshot().toBuilder()
                .lines(List.of(t, t.toBuilder().rowNumber(2).build(), u.toBuilder().rowNumber(3).build()))
                .header(EInvoiceFixtures.header().toBuilder()
                        .amount(EInvoiceFixtures.money("212.48")).grossAmount(EInvoiceFixtures.money("212.48"))
                        .taxAmount(EInvoiceFixtures.money("12.00")).build())
                .build();

        JsonNode inv = invoice(snapshot);
        java.math.BigDecimal headerExclusive = inv.at("/LegalMonetaryTotal/0/LineExtensionAmount/0/_").decimalValue();
        java.math.BigDecimal taxedBase = inv.at("/TaxTotal/0/TaxSubtotal/0/TaxableAmount/0/_").decimalValue();
        java.math.BigDecimal untaxedBase = inv.at("/TaxTotal/0/TaxSubtotal/1/TaxableAmount/0/_").decimalValue();

        assertThat(headerExclusive).isEqualByComparingTo("200.48");
        assertThat(taxedBase.add(untaxedBase)).isEqualByComparingTo(headerExclusive);
        assertThat(untaxedBase).isEqualByComparingTo("0.50");
    }

    @Test
    void untaxedLineUsesTypeSixAndProductNameWhenRemarksBlank() throws Exception {
        JsonNode line = invoice(EInvoiceFixtures.snapshot()).at("/InvoiceLine/1");

        assertThat(line.at("/ID/0/_").asText()).isEqualTo("2");
        assertThat(line.at("/TaxTotal/0/TaxSubtotal/0/TaxCategory/0/ID/0/_").asText()).isEqualTo("06");
        assertThat(line.at("/TaxTotal/0/TaxSubtotal/0/Percent/0/_").decimalValue()).isEqualByComparingTo("0.00");
        assertThat(line.at("/Item/0/Description/0/_").asText()).isEqualTo("DOCUMENTATION FEE");
    }

    @Test
    void documentTotalsReconcileWithTheLines() throws Exception {
        JsonNode inv = invoice(EInvoiceFixtures.snapshot());

        JsonNode totals = inv.at("/LegalMonetaryTotal/0");
        assertThat(totals.at("/LineExtensionAmount/0/_").decimalValue()).isEqualByComparingTo("250.00");
        assertThat(totals.at("/TaxExclusiveAmount/0/_").decimalValue()).isEqualByComparingTo("250.00");
        assertThat(totals.at("/TaxInclusiveAmount/0/_").decimalValue()).isEqualByComparingTo("262.00");
        assertThat(totals.at("/PayableAmount/0/_").decimalValue()).isEqualByComparingTo("262.00");
        assertThat(totals.at("/AllowanceTotalAmount/0/_").decimalValue()).isEqualByComparingTo("0.00");
        assertThat(totals.at("/ChargeTotalAmount/0/_").decimalValue()).isEqualByComparingTo("0.00");
        assertThat(totals.at("/PayableRoundingAmount/0/_").decimalValue()).isEqualByComparingTo("0.00");

        // 200.00 + 50.00 (lines, exclusive) == 250.00 (header) — the reconciliation legacy broke.
        JsonNode lines = inv.at("/InvoiceLine");
        java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
        for (JsonNode line : lines) {
            sum = sum.add(line.at("/LineExtensionAmount/0/_").decimalValue());
        }
        assertThat(sum).isEqualByComparingTo(totals.at("/LineExtensionAmount/0/_").decimalValue());
    }

    @Test
    void documentTaxTotalHasOneSubtotalPerTaxTypePresent() throws Exception {
        JsonNode tax = invoice(EInvoiceFixtures.snapshot()).at("/TaxTotal/0");

        assertThat(tax.at("/TaxAmount/0/_").decimalValue()).isEqualByComparingTo("12.00");
        assertThat(tax.at("/TaxSubtotal")).hasSize(2);

        JsonNode taxed = tax.at("/TaxSubtotal/0");
        assertThat(taxed.at("/TaxCategory/0/ID/0/_").asText()).isEqualTo("01");
        assertThat(taxed.at("/TaxAmount/0/_").decimalValue()).isEqualByComparingTo("12.00");
        assertThat(taxed.at("/TaxableAmount/0/_").decimalValue()).isEqualByComparingTo("200.00");
        assertThat(taxed.at("/Percent/0/_").decimalValue()).isEqualByComparingTo("6.00");

        JsonNode untaxed = tax.at("/TaxSubtotal/1");
        assertThat(untaxed.at("/TaxCategory/0/ID/0/_").asText()).isEqualTo("06");
        assertThat(untaxed.at("/TaxAmount/0/_").decimalValue()).isEqualByComparingTo("0.00");
        assertThat(untaxed.at("/TaxableAmount/0/_").decimalValue()).isEqualByComparingTo("50.00");
    }

    @Test
    void amountsAreWrittenAsPlainTwoDecimalNumbers() {
        String json = codec.toJson(builder.build(EInvoiceFixtures.snapshot(), ISSUED_AT));
        assertThat(json).contains("\"_\":262.00,\"currencyID\":\"MYR\"");
        assertThat(json).doesNotContain("E+").doesNotContain("e+").doesNotContain("null");
    }

    @Test
    void classificationCodesAreThreeDigitsAndZeroIsNeverSent() {
        assertThat(EInvoiceDocumentBuilder.classificationCode(22)).isEqualTo("022");
        assertThat(EInvoiceDocumentBuilder.classificationCode(7)).isEqualTo("007");
        assertThat(EInvoiceDocumentBuilder.classificationCode(null)).isNull();
        assertThat(EInvoiceDocumentBuilder.classificationCode(0)).isNull();
    }

    // ────────────────────────────────────────────────────────────── helpers ──

    private JsonNode json(UblDocument document) throws Exception {
        return reader.readTree(codec.toJson(document));
    }

    private JsonNode invoice(EInvoiceSnapshot snapshot) throws Exception {
        return json(builder.build(snapshot, ISSUED_AT)).at("/Invoice/0");
    }
}
