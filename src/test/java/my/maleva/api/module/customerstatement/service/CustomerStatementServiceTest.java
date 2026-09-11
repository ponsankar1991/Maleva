package my.maleva.api.module.customerstatement.service;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.company.entity.MasterSetting;
import my.maleva.api.module.company.repository.MasterSettingRepository;
import my.maleva.api.module.customerstatement.dto.AgeingBucket;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementLine;
import my.maleva.api.module.customerstatement.dto.StatementRequest;
import my.maleva.api.module.customerstatement.dto.StatementResult;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.AgeingRow;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.CreditNoteKnockoff;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.CustomerHeader;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.OpeningBalance;
import my.maleva.api.module.customerstatement.repository.CustomerStatementQueryRepository.OutstandingInvoice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pins the arithmetic the legacy screen did in the browser — and did wrong.
 * The numbers are ACS FREIGHT's from the sample PDF in malevaold/Exports, so
 * a reader can hold the printed page next to the assertions.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerStatementServiceTest {

    private static final int COMPANY = 6;
    private static final int ACS = 1047;

    @Mock private CustomerStatementQueryRepository queries;
    @Mock private MasterSettingRepository settings;

    private CustomerStatementService service;

    @BeforeEach
    void setUp() {
        service = new CustomerStatementService(queries, settings);
        when(settings.findFirstByCompanyRefIdAndVariableName(anyInt(), any())).thenReturn(Optional.empty());
        when(queries.findCreditNoteKnockoffs(anyInt(), any(), any())).thenReturn(List.of());
        when(queries.findAgeing(anyInt(), any(), any(), any())).thenReturn(List.of());
        when(queries.findOpeningBalances(anyInt(), any(), any())).thenReturn(List.of());
        when(queries.findHeaders(anyInt(), any())).thenReturn(List.of(new CustomerHeader(
                ACS, "ACS FREIGHT SERVICES PTE LTD",
                "119 AIRPORT CARGO ROAD", "#01-03/04 CHANGI CARGO MEGAPLEX 1 SINGAPORE 819454", null,
                "+6590022745", "ACCOUNTS DEPT", "700-A024", "30 DAYS", "SGD",
                "acct@acs.example", "", "ops@acs.example", "acct@acs.example")));
    }

    private static OutstandingInvoice invoice(int id, String date, String no, String desc, String amount) {
        return new OutstandingInvoice(id, ACS, "ACS FREIGHT SERVICES PTE LTD", LocalDate.parse(date),
                no, null, desc, new BigDecimal(amount), BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private static StatementRequest request(boolean creditNotes) {
        StatementRequest r = new StatementRequest();
        r.setCompanyId(COMPANY);
        r.setCustomerId(ACS);
        r.setIncludeCreditNotes(creditNotes);
        return r;
    }

    @Test
    @DisplayName("the running balance starts at zero and lands on the total, as the printed page does")
    void runningBalanceFromZero() {
        when(queries.findOutstandingInvoices(eq(COMPANY), eq(ACS), any(), isNull())).thenReturn(List.of(
                invoice(1, "2026-07-08", "INV000042727", "MADRID EXPRESS", "431.35"),
                invoice(2, "2026-07-10", "INV000042766", "CARPEDIEM - LEE BOAT", "537.43"),
                invoice(3, "2026-07-10", "INV000042767", "SHAMROCK JUPITER", "531.21")));

        CustomerStatement s = service.build(request(false)).getStatements().get(0);

        assertThat(s.getLines()).extracting(StatementLine::balance)
                .containsExactly(new BigDecimal("431.35"), new BigDecimal("968.78"), new BigDecimal("1499.99"));
        assertThat(s.getTotalDebit()).isEqualByComparingTo("1499.99");
        assertThat(s.getTotalCredit()).isEqualByComparingTo("0");
        assertThat(s.getClosingBalance()).isEqualByComparingTo("1499.99");
        // The mail's figures are the same numbers, not a browser-side re-count.
        assertThat(s.getOverdueAmount()).isEqualByComparingTo("1499.99");
        assertThat(s.getOverdueAsOf()).isEqualTo(LocalDate.parse("2026-07-10"));
    }

    @Test
    @DisplayName("the header is the legacy report's aliases, spelled honestly")
    void headerAliases() {
        when(queries.findOutstandingInvoices(eq(COMPANY), eq(ACS), any(), isNull()))
                .thenReturn(List.of(invoice(1, "2026-07-08", "INV000042727", "MADRID EXPRESS", "431.35")));

        CustomerStatement s = service.build(request(false)).getStatements().get(0);

        assertThat(s.getAttn()).isEqualTo("ACCOUNTS DEPT");      // Customer.City
        assertThat(s.getPhone()).isEqualTo("+6590022745");       // OPhone, legacy "Address2"
        assertThat(s.getAccountCode()).isEqualTo("700-A024");    // CompanyCode
        assertThat(s.getTerms()).isEqualTo("30 DAYS");
        assertThat(s.getCurrency()).isEqualTo("SGD");
        // Non-blank, in AEmail/AEmail1/OEmail/OEmail1 order, no duplicates.
        assertThat(s.getEmails()).containsExactly("acct@acs.example", "ops@acs.example");
    }

    @Test
    @DisplayName("a partly paid invoice is listed at what is still owed")
    void partlyPaidInvoiceShowsOutstanding() {
        when(queries.findOutstandingInvoices(eq(COMPANY), eq(ACS), any(), isNull())).thenReturn(List.of(
                new OutstandingInvoice(1, ACS, "ACS", LocalDate.parse("2026-07-08"), "INV1", null, "X",
                        new BigDecimal("1000.00"), new BigDecimal("400.00"), BigDecimal.ZERO)));

        CustomerStatement s = service.build(request(false)).getStatements().get(0);

        assertThat(s.getLines().get(0).debit()).isEqualByComparingTo("600.00");
    }

    @Test
    @DisplayName("with credit notes shown, a note is not counted against its invoice twice")
    void creditNoteCountedOnce() {
        // Legacy: invoice net of the knockoff (800) AND the note as a credit
        // (200) → balance 600 for a customer who owes 800.
        when(queries.findOutstandingInvoices(eq(COMPANY), eq(ACS), any(), isNull())).thenReturn(List.of(
                new OutstandingInvoice(1, ACS, "ACS", LocalDate.parse("2026-07-08"), "INV1", null, "X",
                        new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("200.00"))));
        when(queries.findCreditNoteKnockoffs(eq(COMPANY), eq(ACS), any())).thenReturn(List.of(
                new CreditNoteKnockoff(1, ACS, LocalDate.parse("2026-07-20"), "CN000000123", "INV1", new BigDecimal("200.00")),
                // Against an invoice that is not on this statement: dropped, as legacy's post-filter did.
                new CreditNoteKnockoff(99, ACS, LocalDate.parse("2026-07-21"), "CN000000124", "INV99", new BigDecimal("50.00"))));

        CustomerStatement s = service.build(request(true)).getStatements().get(0);

        assertThat(s.getLines()).hasSize(2);
        assertThat(s.getLines().get(0).debit()).isEqualByComparingTo("1000.00");
        assertThat(s.getLines().get(1).kind()).isEqualTo(StatementLine.Kind.CREDIT_NOTE);
        assertThat(s.getLines().get(1).credit()).isEqualByComparingTo("200.00");
        assertThat(s.getLines().get(1).description()).isEqualTo("INV1");
        assertThat(s.getClosingBalance()).isEqualByComparingTo("800.00");
    }

    @Test
    @DisplayName("the ageing grid is twelve months ending this month, oldest first, zeros filled")
    void ageingGrid() {
        YearMonth now = YearMonth.now();
        when(queries.findOutstandingInvoices(eq(COMPANY), eq(ACS), any(), isNull()))
                .thenReturn(List.of(invoice(1, "2026-07-08", "INV000042727", "MADRID EXPRESS", "431.35")));
        when(queries.findAgeing(eq(COMPANY), eq(ACS), any(), any())).thenReturn(List.of(
                new AgeingRow(ACS, now.getYear(), now.getMonthValue(), new BigDecimal("1076.52")),
                new AgeingRow(ACS, now.minusMonths(1).getYear(), now.minusMonths(1).getMonthValue(), new BigDecimal("8696.72"))));

        List<AgeingBucket> ageing = service.build(request(false)).getStatements().get(0).getAgeing();

        assertThat(ageing).hasSize(12);
        assertThat(ageing.get(0).year()).isEqualTo(now.minusMonths(11).getYear());
        assertThat(ageing.get(0).month()).isEqualTo(now.minusMonths(11).getMonthValue());
        assertThat(ageing.get(11).amount()).isEqualByComparingTo("1076.52");
        assertThat(ageing.get(10).amount()).isEqualByComparingTo("8696.72");
        assertThat(ageing.get(0).amount()).isEqualByComparingTo("0");
        assertThat(ageing.get(11).label()).matches("[A-Z][a-z]{2} \\d{2}");
    }

    @Test
    @DisplayName("the cutoff comes from MasterSetting, else legacy's 2024-10-01")
    void cutoffFromSettings() {
        assertThat(service.cutoffFor(COMPANY)).isEqualTo(LocalDate.of(2024, 10, 1));

        MasterSetting setting = new MasterSetting();
        setting.setSValue("2025-01-01");
        when(settings.findFirstByCompanyRefIdAndVariableName(COMPANY, CustomerStatementService.CUTOFF_SETTING))
                .thenReturn(Optional.of(setting));
        assertThat(service.cutoffFor(COMPANY)).isEqualTo(LocalDate.of(2025, 1, 1));

        setting.setSValue("not a date");
        assertThat(service.cutoffFor(COMPANY)).isEqualTo(LocalDate.of(2024, 10, 1));
    }

    @Test
    @DisplayName("without a date range, invoices are taken from the cutoff onwards with no upper bound")
    void cutoffModeHasNoUpperBound() {
        when(queries.findOutstandingInvoices(anyInt(), any(), any(), any())).thenReturn(List.of());

        StatementResult result = service.build(request(false));

        verify(queries).findOutstandingInvoices(COMPANY, ACS, LocalDate.of(2024, 10, 1), null);
        assertThat(result.getCustomerCount()).isZero();
        assertThat(result.getPeriodTo()).isNull();
    }

    @Test
    @DisplayName("a date range is inclusive of its last day")
    void dateRangeInclusive() {
        when(queries.findOutstandingInvoices(anyInt(), any(), any(), any())).thenReturn(List.of());
        StatementRequest r = request(false);
        r.setUseDateRange(true);
        r.setFromDate(LocalDate.of(2026, 7, 1));
        r.setToDate(LocalDate.of(2026, 7, 31));

        StatementResult result = service.build(r);

        verify(queries).findOutstandingInvoices(COMPANY, ACS, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1));
        assertThat(result.getPeriodTo()).isEqualTo(LocalDate.of(2026, 7, 31));
    }

    @Test
    @DisplayName("From after To is refused, as the legacy screen refused it")
    void fromAfterToRefused() {
        StatementRequest r = request(false);
        r.setUseDateRange(true);
        r.setFromDate(LocalDate.of(2026, 8, 1));
        r.setToDate(LocalDate.of(2026, 7, 1));

        assertThatThrownBy(() -> service.build(r)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    @DisplayName("all-customers mode passes no customer filter and sorts by name")
    void allCustomersSorted() {
        when(queries.findOutstandingInvoices(eq(COMPANY), isNull(), any(), isNull())).thenReturn(List.of(
                new OutstandingInvoice(1, 2, "ZETA LINES", LocalDate.parse("2026-07-01"), "INV1", null, null, new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO),
                new OutstandingInvoice(2, 3, "alpha shipping", LocalDate.parse("2026-07-02"), "INV2", null, null, new BigDecimal("20.00"), BigDecimal.ZERO, BigDecimal.ZERO)));
        when(queries.findHeaders(eq(COMPANY), any())).thenReturn(List.of(
                new CustomerHeader(2, "ZETA LINES", null, null, null, null, null, null, null, null, null, null, null, null),
                new CustomerHeader(3, "alpha shipping", null, null, null, null, null, null, null, null, null, null, null, null)));
        StatementRequest r = new StatementRequest();
        r.setCompanyId(COMPANY);
        r.setCustomerId(0);

        StatementResult result = service.build(r);

        assertThat(result.getCustomerCount()).isEqualTo(2);
        assertThat(result.getStatements()).extracting(CustomerStatement::getCustomerName)
                .containsExactly("alpha shipping", "ZETA LINES");
    }

    @Test
    @DisplayName("a customer with no header row still gets a statement")
    void missingHeaderTolerated() {
        when(queries.findOutstandingInvoices(eq(COMPANY), eq(ACS), any(), isNull()))
                .thenReturn(List.of(invoice(1, "2026-07-08", "INV1", "X", "1.00")));
        when(queries.findHeaders(anyInt(), any())).thenReturn(List.of());

        CustomerStatement s = service.build(request(false)).getStatements().get(0);

        assertThat(s.getCustomerName()).isEqualTo("ACS FREIGHT SERVICES PTE LTD");
        assertThat(s.getEmails()).isEmpty();
        assertThat(s.getOpeningBalance()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("the opening balance from CustomerBalance() starts the running balance, as Crystal's @RuningBalance did")
    void openingStartsTheRunningBalance() {
        when(queries.findOutstandingInvoices(eq(COMPANY), eq(ACS), any(), isNull()))
                .thenReturn(List.of(invoice(1, "2026-07-08", "INV1", "X", "100.00")));
        when(queries.findOpeningBalances(eq(COMPANY), eq(ACS), any()))
                .thenReturn(List.of(new OpeningBalance(ACS, new BigDecimal("5000.00"))));

        CustomerStatement s = service.build(request(false)).getStatements().get(0);

        assertThat(s.getOpeningBalance()).isEqualByComparingTo("5000.00");
        assertThat(s.getLines().get(0).balance()).isEqualByComparingTo("5100.00");   // Opening + Bill - Paid
        assertThat(s.getClosingBalance()).isEqualByComparingTo("5100.00");
        assertThat(s.getOverdueAmount()).isEqualByComparingTo("5100.00");
        assertThat(s.getTotalDebit()).isEqualByComparingTo("100.00");                // totals stay the lines' sums
        // As of the day before the cutoff, whatever range was asked for — legacy's choice.
        verify(queries).findOpeningBalances(COMPANY, ACS, LocalDate.of(2024, 9, 30));
    }
}
