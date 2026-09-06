package my.maleva.api.module.invoice.einvoice;

import my.maleva.api.common.config.MyInvoisProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static my.maleva.api.module.invoice.einvoice.EInvoiceFixtures.money;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The guard that keeps a wrong figure from reaching the government. Each test
 * breaks one thing and expects exactly that thing to be named.
 */
class EInvoiceValidatorTest {

    private MyInvoisProperties properties;
    private EInvoiceValidator validator;

    @BeforeEach
    void setUp() {
        properties = EInvoiceFixtures.properties();
        validator = new EInvoiceValidator(properties);
    }

    @Test
    void consistentInvoicePasses() {
        assertThat(validator.validate(EInvoiceFixtures.snapshot())).isEmpty();
    }

    @Test
    void loadProblemsAreCarriedThrough() {
        EInvoiceSnapshot snapshot = EInvoiceSnapshot.builder()
                .header(EInvoiceFixtures.header())
                .customer(EInvoiceFixtures.customer())
                .lines(List.of(EInvoiceFixtures.taxedLine(), EInvoiceFixtures.untaxedLine()))
                .loadProblems(List.of(EInvoiceProblem.of("line.uom.missing", "Invoice X line 1: uom gone")))
                .build();

        assertThat(codes(snapshot)).containsExactly("line.uom.missing");
    }

    @Test
    void missingTinIsRefusedByName() {
        EInvoiceSnapshot snapshot = withCustomer(EInvoiceFixtures.customer().toBuilder().tin("  ").build());

        List<EInvoiceProblem> problems = validator.validate(snapshot);
        assertThat(codes(problems)).containsExactly("customer.tin.missing");
        assertThat(problems.get(0).message())
                .contains("INV000004711")
                .contains("ACME LOGISTICS SDN BHD")
                .contains("has no TIN");
    }

    @Test
    void missingClassificationIsRefusedBeforeSubmission() {
        // Legacy sent "000" here and the document was validated Invalid, permanently.
        EInvoiceSnapshot snapshot = withLines(
                EInvoiceFixtures.taxedLine().toBuilder().classificationCode(null).build(),
                EInvoiceFixtures.untaxedLine());

        List<EInvoiceProblem> problems = validator.validate(snapshot);
        assertThat(codes(problems)).containsExactly("line.classification.missing");
        assertThat(problems.get(0).message()).contains("line 1 (HANDLING)").contains("Sale Classification");
    }

    @Test
    void lineTaxThatDisagreesWithTheRateIsRefused() {
        // 2 × 100 @ 6% must be 12.00; the row says 11.00 (the line amount 212.00
        // still equals qty × rate + the CORRECT tax, so only the tax rule fires)
        EInvoiceSnapshot snapshot = withLines(
                EInvoiceFixtures.taxedLine().toBuilder().taxAmount(money("11.00")).build(),
                EInvoiceFixtures.untaxedLine());
        // keep the header consistent with the (wrong) line tax so only the line rule fires
        snapshot = withHeader(snapshot, EInvoiceFixtures.header().toBuilder().taxAmount(money("11.00")).build());

        List<EInvoiceProblem> problems = validator.validate(snapshot);
        assertThat(codes(problems)).containsExactly("line.tax.mismatch");
        assertThat(problems.get(0).message()).contains("stored tax 11.00").contains("= 12.00");
    }

    @Test
    void lineWhoseTaxAndAmountAreBothWrongReportsBoth() {
        EInvoiceSnapshot snapshot = withLines(
                EInvoiceFixtures.taxedLine().toBuilder().taxAmount(money("11.00")).amount(money("211.00")).build(),
                EInvoiceFixtures.untaxedLine());
        snapshot = withHeader(snapshot, EInvoiceFixtures.header().toBuilder()
                .amount(money("261.00")).grossAmount(money("261.00")).taxAmount(money("11.00")).build());

        assertThat(codes(validator.validate(snapshot)))
                .containsExactlyInAnyOrder("line.tax.mismatch", "line.amount.mismatch");
    }

    @Test
    void headerThatDisagreesWithTheLinesIsRefusedWithTheDifference() {
        EInvoiceSnapshot snapshot = withHeader(EInvoiceFixtures.snapshot(), EInvoiceFixtures.header().toBuilder()
                .amount(money("262.05")).grossAmount(money("262.05")).build());

        List<EInvoiceProblem> problems = validator.validate(snapshot);
        assertThat(codes(problems)).containsExactly("header.amount.mismatch");
        assertThat(problems.get(0).message())
                .contains("header amount 262.05")
                .contains("sum of 2 lines 262.00")
                .contains("difference 0.05");
    }

    @Test
    void oneSenOfFloat32DriftIsTolerated() {
        // Line tax is stored unrounded in a float32 column, so the header sum
        // and the rounded lines can differ by a sen without anything being wrong.
        EInvoiceSnapshot snapshot = withHeader(EInvoiceFixtures.snapshot(), EInvoiceFixtures.header().toBuilder()
                .amount(money("262.01")).grossAmount(money("262.01")).taxAmount(money("12.01")).build());
        assertThat(validator.validate(snapshot)).isEmpty();
    }

    @Test
    void amountsTheFloat32ColumnCannotHoldToTheSenAreRefusedEvenWhenTheyReconcile() {
        // 2 × 100,000 @ 0% = 200,000.00: at that size the column's step is 0.0156,
        // so the stored sen is a guess; reconciling proves nothing.
        EInvoiceSnapshot.Line big = EInvoiceFixtures.untaxedLine().toBuilder()
                .quantity(money("2.00")).unitPrice(money("100000.00")).amount(money("200000.00")).build();
        EInvoiceSnapshot snapshot = withLines(big);
        snapshot = withHeader(snapshot, EInvoiceFixtures.header().toBuilder()
                .amount(money("200000.00")).grossAmount(money("200000.00")).taxAmount(money("0.00")).build());

        List<EInvoiceProblem> problems = validator.validate(snapshot);
        assertThat(codes(problems)).containsExactlyInAnyOrder("header.amount.imprecise", "line.amount.imprecise");
        assertThat(problems.get(0).message()).contains("131072").contains("cannot hold");
    }

    @Test
    void zeroRateLineWithAStoredTaxIsRefused() {
        EInvoiceSnapshot snapshot = withLines(
                EInvoiceFixtures.taxedLine(),
                EInvoiceFixtures.untaxedLine().toBuilder().taxAmount(money("0.01")).build());
        snapshot = withHeader(snapshot, EInvoiceFixtures.header().toBuilder().taxAmount(money("12.01")).build());

        assertThat(codes(validator.validate(snapshot))).containsExactly("line.tax.without.rate");
    }

    @Test
    void classificationCodeZeroIsTreatedAsMissing() {
        // legacy sent "000" for exactly this and LHDN marked the document Invalid
        EInvoiceSnapshot snapshot = withLines(
                EInvoiceFixtures.taxedLine().toBuilder().classificationCode(0).build(),
                EInvoiceFixtures.untaxedLine());
        assertThat(codes(validator.validate(snapshot))).containsExactly("line.classification.missing");
    }

    @Test
    void headerSumsAreNotCheckedWhenALineIsIncomplete() {
        EInvoiceSnapshot snapshot = withLines(
                EInvoiceFixtures.taxedLine().toBuilder().amount(null).build(),
                EInvoiceFixtures.untaxedLine());
        assertThat(codes(validator.validate(snapshot))).containsExactly("line.value.missing");
    }

    @Test
    void mixedTaxRatesAreRefused() {
        EInvoiceSnapshot.Line eightPercent = EInvoiceFixtures.untaxedLine().toBuilder()
                .taxPercent(money("8.00")).taxAmount(money("4.00")).amount(money("54.00")).build();
        EInvoiceSnapshot snapshot = withLines(EInvoiceFixtures.taxedLine(), eightPercent);
        snapshot = withHeader(snapshot, EInvoiceFixtures.header().toBuilder()
                .amount(money("266.00")).grossAmount(money("266.00")).taxAmount(money("16.00")).build());

        assertThat(codes(validator.validate(snapshot))).containsExactly("lines.tax.rates.mixed");
    }

    @Test
    void foreignCurrencyPassesByDefaultAndCanBeHeldBack() {
        // 40% of e-invoiced sales are SGD/USD and LHDN validated them as sent.
        EInvoiceSnapshot snapshot = withCustomer(EInvoiceFixtures.customer().toBuilder().currencyCode("SGD").build());
        assertThat(validator.validate(snapshot)).isEmpty();

        properties.setAllowForeignCurrency(false);
        assertThat(codes(validator.validate(snapshot))).containsExactly("currency.foreign.disabled");
    }

    @Test
    void junkCurrencyIsRefusedAsSuch() {
        EInvoiceSnapshot snapshot = withCustomer(EInvoiceFixtures.customer().toBuilder().currencyCode("S").build());
        assertThat(codes(validator.validate(snapshot))).containsExactly("customer.currency.invalid");
    }

    @Test
    void unknownMalaysianStateIsRefused() {
        EInvoiceSnapshot snapshot = withCustomer(EInvoiceFixtures.customer().toBuilder().state("Atlantis").build());
        assertThat(codes(validator.validate(snapshot))).containsExactly("customer.state.unknown");
    }

    @Test
    void cancelledInvoiceIsRefused() {
        EInvoiceSnapshot snapshot = withHeader(EInvoiceFixtures.snapshot(),
                EInvoiceFixtures.header().toBuilder().active(false).build());
        assertThat(codes(validator.validate(snapshot))).containsExactly("invoice.inactive");
    }

    @Test
    void negativeAndMissingLineValuesAreRefused() {
        EInvoiceSnapshot snapshot = withLines(
                EInvoiceFixtures.taxedLine().toBuilder().quantity(money("-2.00")).build(),
                EInvoiceFixtures.untaxedLine().toBuilder().unitPrice(null).build());

        assertThat(codes(validator.validate(snapshot)))
                .contains("line.value.negative", "line.value.missing");
    }

    @Test
    void everyProblemIsReportedAtOnce() {
        EInvoiceSnapshot snapshot = withCustomer(EInvoiceFixtures.customer().toBuilder()
                .tin("").phone("").city("").postalZone("").build());

        assertThat(codes(validator.validate(snapshot))).containsExactlyInAnyOrder(
                "customer.tin.missing", "customer.phone.missing", "customer.city.missing", "customer.postcode.missing");
    }

    // ────────────────────────────────────────────────────────────── helpers ──

    private static List<String> codes(List<EInvoiceProblem> problems) {
        return problems.stream().map(EInvoiceProblem::code).toList();
    }

    private List<String> codes(EInvoiceSnapshot snapshot) {
        return codes(validator.validate(snapshot));
    }

    private static EInvoiceSnapshot withCustomer(EInvoiceSnapshot.Customer customer) {
        return EInvoiceFixtures.snapshot().toBuilder().customer(customer).build();
    }

    private static EInvoiceSnapshot withLines(EInvoiceSnapshot.Line... lines) {
        return EInvoiceFixtures.snapshot().toBuilder().lines(List.of(lines)).build();
    }

    private static EInvoiceSnapshot withHeader(EInvoiceSnapshot snapshot, EInvoiceSnapshot.Header header) {
        return snapshot.toBuilder().header(header).build();
    }
}
