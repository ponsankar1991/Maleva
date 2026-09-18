package my.maleva.api.module.customerstatement.print;

import my.maleva.api.module.customerstatement.dto.AgeingBucket;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementLine;
import my.maleva.api.module.customerstatement.dto.StatementResult;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The Statement of Account as an Excel workbook - the "Excel" report type
 * the legacy screen offered, and the attachment for customers who reconcile
 * in a spreadsheet.
 *
 * <p>Same statement object the PDF prints, so the figures are identical: the
 * letterhead, the customer block, one row per line (DATE, INVOICE NO,
 * REFERENCE, DESCRIPTION, DEBIT, CREDIT, BALANCE) with real numbers, not text,
 * the totals, the closing balance, the ageing by month and the two notices.
 * One sheet per customer, so an all-customers export is one file.
 */
@Service
public class CustomerStatementExcelService {

    public static final String CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private static final DateTimeFormatter LINE_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter STATEMENT_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final String[] HEADS = {"DATE", "INVOICE NO", "REFERENCE", "DESCRIPTION", "DEBIT", "CREDIT", "BALANCE"};
    private static final int LAST_COL = HEADS.length - 1;

    /** A rendered workbook: its file name and bytes. */
    public record RenderedWorkbook(String fileName, byte[] content) {
    }

    public RenderedWorkbook render(StatementResult result) {
        try (XSSFWorkbook book = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles styles = new Styles(book);
            Set<String> sheetNames = new HashSet<>();
            List<CustomerStatement> statements = result.getStatements() == null ? List.of() : result.getStatements();
            if (statements.isEmpty()) {
                book.createSheet("Statement").createRow(0).createCell(0).setCellValue("No outstanding invoices for these filters.");
            }
            for (CustomerStatement s : statements) {
                writeSheet(book.createSheet(sheetName(s.getCustomerName(), sheetNames)), s, result, styles);
            }
            book.write(out);
            return new RenderedWorkbook(fileNameFor(result), out.toByteArray());
        } catch (IOException ex) {
            throw new IllegalStateException("The customer statement workbook could not be written", ex);
        }
    }

    /**
     * One customer's workbook out of a many-customer result - the attachment
     * for that customer's mail in a run. Same narrowing as the PDF's
     * {@code renderOne}: the result's period and ageing window, one statement.
     */
    public RenderedWorkbook renderOne(StatementResult whole, CustomerStatement statement) {
        return render(StatementResult.builder()
                .statements(List.of(statement))
                .customerCount(1)
                .lineCount(statement.getLines() == null ? 0 : statement.getLines().size())
                .cutoffDate(whole.getCutoffDate())
                .periodFrom(whole.getPeriodFrom())
                .periodTo(whole.getPeriodTo())
                .ageingFrom(whole.getAgeingFrom())
                .ageingTo(whole.getAgeingTo())
                .build());
    }

    /** The PDF's name with the Excel extension: CustomerStatement_&lt;customer&gt;_&lt;yyyyMMdd&gt;.xlsx. */
    public static String fileNameFor(StatementResult result) {
        String pdf = CustomerStatementPdfService.fileNameFor(result);
        return pdf.substring(0, pdf.length() - ".pdf".length()) + ".xlsx";
    }

    private void writeSheet(Sheet sheet, CustomerStatement s, StatementResult result, Styles st) {
        int r = 0;
        r = text(sheet, r, CustomerStatementPdfService.COMPANY, st.company);
        r = text(sheet, r, CustomerStatementPdfService.REG_NO, st.small);
        r = text(sheet, r, CustomerStatementPdfService.ADDRESS1, st.small);
        r = text(sheet, r, CustomerStatementPdfService.ADDRESS2, st.small);
        r = text(sheet, r, CustomerStatementPdfService.ADDRESS3, st.small);
        r = text(sheet, r, CustomerStatementPdfService.ADDRESS4, st.small);
        r++;
        r = text(sheet, r, CustomerStatementPdfService.TITLE, st.title);
        r++;

        String currency = blank(s.getCurrency());
        r = pair(sheet, r, "Customer", s.getCustomerName(), "Date", format(s.getStatementDate(), STATEMENT_DATE), st);
        r = pair(sheet, r, "Address", join(s.getAddress1(), s.getAddress2(), s.getAddress3()), "A/C Code", blank(s.getAccountCode()), st);
        r = pair(sheet, r, "Attn", blank(s.getAttn()), "Terms", blank(s.getTerms()), st);
        r = pair(sheet, r, "Tel", blank(s.getPhone()), "Currency", currency, st);
        String period = result.getPeriodTo() != null
                ? format(result.getPeriodFrom(), STATEMENT_DATE) + " to " + format(result.getPeriodTo(), STATEMENT_DATE)
                : "From " + format(result.getPeriodFrom(), STATEMENT_DATE);
        r = pair(sheet, r, "Period", period, "", "", st);
        r++;

        Row head = sheet.createRow(r++);
        for (int c = 0; c < HEADS.length; c++) {
            cell(head, c, HEADS[c], c >= 4 ? st.headRight : st.head);
        }
        int firstLineRow = r;

        BigDecimal opening = nz(s.getOpeningBalance());
        if (opening.signum() != 0) {
            Row row = sheet.createRow(r++);
            cell(row, 3, "OPENING BALANCE", st.bold);
            number(row, 6, opening, st.money);
        }
        for (StatementLine line : s.getLines() == null ? List.<StatementLine>of() : s.getLines()) {
            Row row = sheet.createRow(r++);
            cell(row, 0, format(line.date(), LINE_DATE), st.plain);
            cell(row, 1, blank(line.documentNo()), st.plain);
            cell(row, 2, blank(line.reference()), st.plain);
            cell(row, 3, blank(line.description()), st.plain);
            numberOrBlank(row, 4, line.debit(), st.money);
            numberOrBlank(row, 5, line.credit(), st.money);
            number(row, 6, nz(line.balance()), st.money);
        }
        if (r == firstLineRow) {
            cell(sheet.createRow(r++), 3, "No outstanding invoices", st.plain);
        }

        Row totals = sheet.createRow(r++);
        cell(totals, 3, "TOTAL " + currency, st.totalLabel);
        number(totals, 4, nz(s.getTotalDebit()), st.totalMoney);
        number(totals, 5, nz(s.getTotalCredit()), st.totalMoney);
        number(totals, 6, nz(s.getClosingBalance()), st.totalMoney);
        r++;

        r = amount(sheet, r, "CLOSING BALANCE " + currency, s.getClosingBalance(), st);
        r = amount(sheet, r, "OVERDUE " + currency, s.getOverdueAmount(), st);
        r++;

        List<AgeingBucket> ageing = s.getAgeing() == null ? List.of() : s.getAgeing();
        if (!ageing.isEmpty()) {
            Row labels = sheet.createRow(r++);
            cell(labels, 0, "AGEING", st.bold);
            Row amounts = sheet.createRow(r++);
            // chronological, twelve months: two rows of label / amount pairs would
            // not fit seven columns, so months run down in pairs of columns
            int col = 1;
            Row labelRow = labels;
            Row amountRow = amounts;
            for (AgeingBucket b : ageing) {
                if (col > LAST_COL) {
                    labelRow = sheet.createRow(r++);
                    amountRow = sheet.createRow(r++);
                    col = 1;
                }
                cell(labelRow, col, b.label(), st.headRight);
                number(amountRow, col, nz(b.amount()), st.money);
                col++;
            }
            r++;
        }

        r = text(sheet, r, CustomerStatementPdfService.NOTE1, st.note);
        text(sheet, r, CustomerStatementPdfService.NOTE2, st.note);

        sheet.setColumnWidth(0, 12 * 256);
        sheet.setColumnWidth(1, 17 * 256);
        sheet.setColumnWidth(2, 22 * 256);
        sheet.setColumnWidth(3, 40 * 256);
        sheet.setColumnWidth(4, 15 * 256);
        sheet.setColumnWidth(5, 15 * 256);
        sheet.setColumnWidth(6, 16 * 256);
        sheet.createFreezePane(0, firstLineRow);
        sheet.getPrintSetup().setLandscape(false);
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);
    }

    // ── cells ───────────────────────────────────────────────────────────────

    private static int text(Sheet sheet, int r, String value, CellStyle style) {
        Row row = sheet.createRow(r);
        cell(row, 0, value, style);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, LAST_COL));
        return r + 1;
    }

    private static int pair(Sheet sheet, int r, String label, String value, String label2, String value2, Styles st) {
        Row row = sheet.createRow(r);
        cell(row, 0, label, st.bold);
        cell(row, 1, value, st.plain);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 3));
        if (!label2.isEmpty()) {
            cell(row, 4, label2, st.bold);
            cell(row, 5, value2, st.plain);
            sheet.addMergedRegion(new CellRangeAddress(r, r, 5, 6));
        }
        return r + 1;
    }

    private static int amount(Sheet sheet, int r, String label, BigDecimal value, Styles st) {
        Row row = sheet.createRow(r);
        cell(row, 3, label, st.totalLabel);
        number(row, 6, nz(value), st.totalMoney);
        return r + 1;
    }

    private static void cell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private static void number(Row row, int col, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value.doubleValue());
        cell.setCellStyle(style);
    }

    private static void numberOrBlank(Row row, int col, BigDecimal value, CellStyle style) {
        if (value == null || value.signum() == 0) {
            row.createCell(col).setCellStyle(style);
        } else {
            number(row, col, value, style);
        }
    }

    /** Excel sheet names: at most 31 characters, none of []:*?/\ and unique in the book. */
    static String sheetName(String customerName, Set<String> used) {
        String base = WorkbookUtil.createSafeSheetName(blank(customerName).isBlank() ? "Statement" : customerName.trim());
        if (base.length() > 31) {
            base = base.substring(0, 31);
        }
        String name = base;
        for (int n = 2; used.contains(name.toLowerCase(Locale.ROOT)); n++) {
            String suffix = " (" + n + ")";
            name = base.substring(0, Math.min(base.length(), 31 - suffix.length())) + suffix;
        }
        used.add(name.toLowerCase(Locale.ROOT));
        return name;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static String blank(String value) {
        return value == null ? "" : value.trim();
    }

    private static String join(String... parts) {
        StringBuilder out = new StringBuilder();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                if (!out.isEmpty()) {
                    out.append(", ");
                }
                out.append(part.trim());
            }
        }
        return out.toString();
    }

    private static String format(LocalDate date, DateTimeFormatter formatter) {
        return date == null ? "" : date.format(formatter);
    }

    /** The handful of cell styles, made once per workbook (Excel caps a book at ~64k styles). */
    private static final class Styles {
        final CellStyle company;
        final CellStyle title;
        final CellStyle small;
        final CellStyle plain;
        final CellStyle bold;
        final CellStyle head;
        final CellStyle headRight;
        final CellStyle money;
        final CellStyle totalLabel;
        final CellStyle totalMoney;
        final CellStyle note;

        Styles(XSSFWorkbook book) {
            DataFormat formats = book.createDataFormat();
            short moneyFormat = formats.getFormat("#,##0.00;-#,##0.00;\"\"");

            company = style(book, font(book, 14, true, false), HorizontalAlignment.CENTER);
            title = style(book, font(book, 12, true, false), HorizontalAlignment.CENTER);
            small = style(book, font(book, 9, false, false), HorizontalAlignment.CENTER);
            plain = style(book, font(book, 10, false, false), HorizontalAlignment.LEFT);
            bold = style(book, font(book, 10, true, false), HorizontalAlignment.LEFT);
            note = style(book, font(book, 9, false, true), HorizontalAlignment.LEFT);

            head = style(book, font(book, 10, true, false), HorizontalAlignment.LEFT);
            head.setBorderTop(BorderStyle.MEDIUM);
            head.setBorderBottom(BorderStyle.THIN);
            head.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            head.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headRight = book.createCellStyle();
            headRight.cloneStyleFrom(head);
            headRight.setAlignment(HorizontalAlignment.RIGHT);

            money = style(book, font(book, 10, false, false), HorizontalAlignment.RIGHT);
            money.setDataFormat(moneyFormat);

            totalLabel = style(book, font(book, 10, true, false), HorizontalAlignment.RIGHT);
            totalLabel.setBorderTop(BorderStyle.THIN);
            totalMoney = style(book, font(book, 10, true, false), HorizontalAlignment.RIGHT);
            totalMoney.setDataFormat(formats.getFormat("#,##0.00"));
            totalMoney.setBorderTop(BorderStyle.THIN);
        }

        private static Font font(XSSFWorkbook book, int points, boolean bold, boolean italic) {
            Font font = book.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) points);
            font.setBold(bold);
            font.setItalic(italic);
            return font;
        }

        private static CellStyle style(XSSFWorkbook book, Font font, HorizontalAlignment align) {
            CellStyle style = book.createCellStyle();
            style.setFont(font);
            style.setAlignment(align);
            return style;
        }
    }
}
