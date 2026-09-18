package my.maleva.api.module.customerstatement.print;

import my.maleva.api.module.customerstatement.dto.AgeingBucket;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementLine;
import my.maleva.api.module.customerstatement.dto.StatementResult;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** The Excel statement, read back with POI: one sheet per customer, real numbers, the totals the PDF prints. */
class CustomerStatementExcelServiceTest {

    private final CustomerStatementExcelService service = new CustomerStatementExcelService();

    private static CustomerStatement statement(int id, String name) {
        return CustomerStatement.builder()
                .customerId(id).customerName(name).currency("SGD").emails(List.of())
                .address1("1 HARBOUR ROAD").attn("CAYDEN").accountCode("300-A001").terms("30 DAYS")
                .statementDate(LocalDate.of(2026, 9, 17))
                .lines(List.of(
                        new StatementLine(StatementLine.Kind.INVOICE, LocalDate.of(2026, 8, 3), "INV000030774", "PO-1", "MV ORIENT",
                                new BigDecimal("1000.00"), BigDecimal.ZERO, new BigDecimal("1000.00")),
                        new StatementLine(StatementLine.Kind.CREDIT_NOTE, LocalDate.of(2026, 8, 9), "CN000000012", null, "INV000030774",
                                BigDecimal.ZERO, new BigDecimal("200.00"), new BigDecimal("800.00"))))
                .totalDebit(new BigDecimal("1000.00")).totalCredit(new BigDecimal("200.00"))
                .closingBalance(new BigDecimal("800.00")).openingBalance(BigDecimal.ZERO)
                .overdueAmount(new BigDecimal("800.00"))
                .ageing(List.of(new AgeingBucket(2026, 8, "Aug", "Aug 26", new BigDecimal("800.00"))))
                .build();
    }

    private static StatementResult result(CustomerStatement... statements) {
        return StatementResult.builder().statements(List.of(statements)).customerCount(statements.length)
                .lineCount(2 * statements.length).cutoffDate(LocalDate.of(2024, 10, 1))
                .periodFrom(LocalDate.of(2024, 10, 1)).build();
    }

    private static Row rowWith(Sheet sheet, int col, String text) {
        for (Row row : sheet) {
            if (row.getCell(col) != null && text.equals(row.getCell(col).toString())) {
                return row;
            }
        }
        throw new AssertionError("no row with " + text);
    }

    @Test
    void oneCustomerIsOneSheetWithTheLinesAsNumbersAndTheTotals() throws Exception {
        var workbook = service.render(result(statement(17, "ACS FREIGHT & SERVICES PTE LTD")));

        assertThat(workbook.fileName()).startsWith("CustomerStatement_ACS_FREIGHT_SERVICES_PTE_LTD_").endsWith(".xlsx");
        try (var book = new XSSFWorkbook(new ByteArrayInputStream(workbook.content()))) {
            assertThat(book.getNumberOfSheets()).isEqualTo(1);
            Sheet sheet = book.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("MALEVA (M) SDN BHD");

            Row invoice = rowWith(sheet, 1, "INV000030774");
            assertThat(invoice.getCell(0).getStringCellValue()).isEqualTo("03/08/2026");
            assertThat(invoice.getCell(4).getNumericCellValue()).isEqualTo(1000.00);
            assertThat(invoice.getCell(6).getNumericCellValue()).isEqualTo(1000.00);

            Row credit = rowWith(sheet, 1, "CN000000012");
            assertThat(credit.getCell(5).getNumericCellValue()).isEqualTo(200.00);
            assertThat(credit.getCell(6).getNumericCellValue()).isEqualTo(800.00);

            Row totals = rowWith(sheet, 3, "TOTAL SGD");
            assertThat(totals.getCell(4).getNumericCellValue()).isEqualTo(1000.00);
            assertThat(totals.getCell(5).getNumericCellValue()).isEqualTo(200.00);
            assertThat(rowWith(sheet, 3, "CLOSING BALANCE SGD").getCell(6).getNumericCellValue()).isEqualTo(800.00);
            assertThat(rowWith(sheet, 1, "Aug 26")).isNotNull();
        }
    }

    @Test
    void everyCustomerGetsTheirOwnUniquelyNamedSheet() throws Exception {
        var workbook = service.render(result(statement(1, "WAN HAI LINES"), statement(2, "WAN HAI LINES"),
                statement(3, "A/B: [SHIPPING]*")));

        assertThat(workbook.fileName()).startsWith("CustomerStatement_All_");
        try (var book = new XSSFWorkbook(new ByteArrayInputStream(workbook.content()))) {
            assertThat(book.getNumberOfSheets()).isEqualTo(3);
            assertThat(book.getSheetName(0)).isEqualTo("WAN HAI LINES");
            assertThat(book.getSheetName(1)).isEqualTo("WAN HAI LINES (2)");
            assertThat(book.getSheetName(2)).doesNotContain("/", ":", "[", "]", "*");
        }
    }

    @Test
    void sheetNamesStayWithinExcelsThirtyOneCharacters() {
        Set<String> used = new HashSet<>();
        String first = CustomerStatementExcelService.sheetName("AIT WORLDWIDE LOGISTICS (MALAYSIA) SDN BHD", used);
        String second = CustomerStatementExcelService.sheetName("AIT WORLDWIDE LOGISTICS (MALAYSIA) SDN BHD", used);
        assertThat(first).hasSizeLessThanOrEqualTo(31);
        assertThat(second).hasSizeLessThanOrEqualTo(31).endsWith("(2)").isNotEqualTo(first);
    }
}
