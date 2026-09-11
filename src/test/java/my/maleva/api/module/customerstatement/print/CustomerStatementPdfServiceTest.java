package my.maleva.api.module.customerstatement.print;

import my.maleva.api.module.customerstatement.dto.AgeingBucket;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementLine;
import my.maleva.api.module.customerstatement.dto.StatementResult;
import my.maleva.api.module.customerstatement.print.CustomerStatementPdfService.RenderedStatement;
import my.maleva.api.module.paymentrecept.print.ReportFonts;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Fills the real template and reads the PDF back. This is what proves the
 * jrxml compiles, the field names match {@link StatementPrintRow}, the group
 * breaks per customer, and the page numbers restart — none of which a unit
 * test on the service can see. The figures are ACS FREIGHT's from the sample
 * Crystal export.
 */
class CustomerStatementPdfServiceTest {

    private static CustomerStatementPdfService service;

    @BeforeAll
    static void compileOnce() {
        service = new CustomerStatementPdfService(new ReportFonts());
    }

    private static List<AgeingBucket> ageing(BigDecimal thisMonth, BigDecimal lastMonth) {
        YearMonth now = YearMonth.now();
        List<AgeingBucket> buckets = new ArrayList<>();
        for (int back = 11; back >= 0; back--) {
            YearMonth ym = now.minusMonths(back);
            BigDecimal amount = back == 0 ? thisMonth : back == 1 ? lastMonth : BigDecimal.ZERO;
            buckets.add(new AgeingBucket(ym.getYear(), ym.getMonthValue(),
                    ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    ym.format(DateTimeFormatter.ofPattern("MMM yy", Locale.ENGLISH)), amount));
        }
        return buckets;
    }

    private static CustomerStatement acs(int lineCount) {
        List<StatementLine> lines = new ArrayList<>();
        BigDecimal running = BigDecimal.ZERO;
        for (int i = 0; i < lineCount; i++) {
            BigDecimal debit = new BigDecimal("431.35");
            running = running.add(debit);
            lines.add(new StatementLine(StatementLine.Kind.INVOICE, LocalDate.of(2026, 7, 8).plusDays(i),
                    "INV0000427" + (27 + i), null, i == 0 ? "MADRID EXPRESS" : "DONOUSA",
                    debit, BigDecimal.ZERO, running));
        }
        return CustomerStatement.builder()
                .customerId(1047)
                .customerName("ACS FREIGHT SERVICES PTE LTD")
                .address1("119 AIRPORT CARGO ROAD")
                .address2("#01-03/04 CHANGI CARGO MEGAPLEX 1 SINGAPORE 819454")
                .phone("+6590022745")
                .attn("ACCOUNTS DEPT")
                .accountCode("700-A024")
                .terms("30 DAYS")
                .currency("SGD")
                .emails(List.of())
                .statementDate(LocalDate.of(2026, 8, 12))
                .lines(lines)
                .totalDebit(running)
                .totalCredit(BigDecimal.ZERO)
                .closingBalance(running)
                .ageing(ageing(new BigDecimal("1076.52"), new BigDecimal("8696.72")))
                .openingBalance(BigDecimal.ZERO)
                .overdueAmount(running)
                .overdueAsOf(lines.get(lines.size() - 1).date())
                .build();
    }

    private static StatementResult result(CustomerStatement... statements) {
        return StatementResult.builder()
                .statements(List.of(statements))
                .customerCount(statements.length)
                .lineCount(List.of(statements).stream().mapToInt(s -> s.getLines().size()).sum())
                .cutoffDate(LocalDate.of(2024, 10, 1))
                .periodFrom(LocalDate.of(2024, 10, 1))
                .ageingFrom(YearMonth.now().minusMonths(11).atDay(1))
                .ageingTo(YearMonth.now().atEndOfMonth())
                .build();
    }

    private static String text(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(document);
        }
    }

    private static int pages(byte[] pdf) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return document.getNumberOfPages();
        }
    }

    @Test
    @DisplayName("renders the sample statement: letterhead, customer block, lines, totals, ageing")
    void rendersSampleStatement() throws IOException {
        RenderedStatement rendered = service.render(result(acs(3)));

        assertThat(rendered.fileName()).startsWith("CustomerStatement_ACS_FREIGHT_SERVICES_PTE_LTD_").endsWith(".pdf");
        assertThat(new String(rendered.pdf(), 0, 4)).isEqualTo("%PDF");

        String text = text(rendered.pdf());
        assertThat(text).contains("MALEVA (M) SDN BHD");
        assertThat(text).contains("STATEMENT OF ACCOUNT");
        assertThat(text).contains("ACS FREIGHT SERVICES PTE LTD");
        assertThat(text).contains("ACCOUNTS DEPT");          // attn = Customer.City
        assertThat(text).contains("+6590022745");            // phone, legacy "Address2"
        assertThat(text).contains("700-A024");               // A/C CODE = QNE code
        assertThat(text).contains("30 DAYS");
        assertThat(text).contains("12/8/2026");              // statement date, unpadded as printed
        assertThat(text).contains("08-07-2026");             // line date
        assertThat(text).contains("INV000042727");
        assertThat(text).contains("MADRID EXPRESS");
        assertThat(text).contains("431.35");
        assertThat(text).contains("1,294.05");               // 3 x 431.35, the running balance and total
        assertThat(text).contains("SGD");
        assertThat(text).contains("8,696.72");               // last month's ageing cell
        assertThat(text).contains("1,076.52");
        assertThat(text).contains("WE SHALL BE GRATEFUL");
        assertThat(text).contains("PREPARED BY:");
        assertThat(text).contains("APPROVED BY:");
        assertThat(text).contains("Page 1 of 1");
        assertThat(pages(rendered.pdf())).isEqualTo(1);
    }

    @Test
    @DisplayName("each customer starts a new page and its pages are numbered from 1")
    void oneCustomerPerPageRun() throws IOException {
        CustomerStatement second = CustomerStatement.builder()
                .customerId(2).customerName("ZETA LINES SDN BHD").currency("MYR").emails(List.of())
                .statementDate(LocalDate.of(2026, 8, 12))
                .lines(List.of(new StatementLine(StatementLine.Kind.INVOICE, LocalDate.of(2026, 8, 1),
                        "INV000050001", "PO-77", "MV SOMETHING", new BigDecimal("900.00"), BigDecimal.ZERO, new BigDecimal("900.00"))))
                .totalDebit(new BigDecimal("900.00")).totalCredit(BigDecimal.ZERO).closingBalance(new BigDecimal("900.00"))
                .ageing(ageing(new BigDecimal("900.00"), BigDecimal.ZERO))
                .openingBalance(BigDecimal.ZERO).overdueAmount(new BigDecimal("900.00")).overdueAsOf(LocalDate.of(2026, 8, 1))
                .build();

        RenderedStatement rendered = service.render(result(acs(2), second));

        assertThat(rendered.fileName()).startsWith("CustomerStatement_All_");
        assertThat(pages(rendered.pdf())).isEqualTo(2);
        try (PDDocument document = Loader.loadPDF(rendered.pdf())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1); stripper.setEndPage(1);
            String page1 = stripper.getText(document);
            stripper.setStartPage(2); stripper.setEndPage(2);
            String page2 = stripper.getText(document);

            assertThat(page1).contains("ACS FREIGHT").doesNotContain("ZETA LINES");
            assertThat(page2).contains("ZETA LINES").doesNotContain("ACS FREIGHT");
            // Pages are numbered through the whole report, as Crystal numbered them.
            assertThat(page2).contains("Page 2 of 2");
            assertThat(page2).contains("PO-77");
        }
    }

    @Test
    @DisplayName("a long statement flows onto a second page: column heads and page number, no letterhead or customer block")
    void longStatementPaginates() throws IOException {
        RenderedStatement rendered = service.render(result(acs(60)));

        assertThat(pages(rendered.pdf())).isGreaterThanOrEqualTo(2);
        try (PDDocument document = Loader.loadPDF(rendered.pdf())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(1); stripper.setEndPage(1);
            String page1 = stripper.getText(document);
            stripper.setStartPage(2); stripper.setEndPage(2);
            String page2 = stripper.getText(document);
            assertThat(page1).contains("STATEMENT OF ACCOUNT").contains("ACS FREIGHT SERVICES PTE LTD").contains("Page 1 of");
            assertThat(page2).contains("Page 2 of").contains("INVOICE NO");          // continuation: heads + page number
            assertThat(page2).doesNotContain("STATEMENT OF ACCOUNT")               // letterhead once per customer
                    .doesNotContain("ACS FREIGHT SERVICES PTE LTD");                // customer block once per customer
        }
    }

    @Test
    @DisplayName("when only the totals block spills to a new page, that page has no column headings")
    void footerOnlyPageHasNoColumnHeads() throws IOException {
        // 32 lines fill page 1 past the point where the bottom-pinned footer fits under them
        RenderedStatement rendered = service.render(result(acs(32)));

        assertThat(pages(rendered.pdf())).isEqualTo(2);
        try (PDDocument document = Loader.loadPDF(rendered.pdf())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(2); stripper.setEndPage(2);
            String page2 = stripper.getText(document);
            assertThat(page2).contains("Page 2 of 2").contains("PREPARED BY:").contains("SGD");
            assertThat(page2).doesNotContain("INV0000427");                    // no lines on it
            assertThat(page2).doesNotContain("INVOICE NO");                    // so no headings either
        }
    }

    @Test
    @DisplayName("an empty result is refused rather than rendered as a page-less PDF")
    void emptyResultRefused() {
        assertThatThrownBy(() -> service.render(result()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nothing to print");
    }
}
