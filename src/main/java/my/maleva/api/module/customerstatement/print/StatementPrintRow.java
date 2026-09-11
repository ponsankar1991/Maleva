package my.maleva.api.module.customerstatement.print;

import lombok.Builder;
import lombok.Getter;
import my.maleva.api.module.customerstatement.dto.AgeingBucket;
import my.maleva.api.module.customerstatement.dto.CustomerStatement;
import my.maleva.api.module.customerstatement.dto.StatementLine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * One statement line flattened for the Jasper data source, carrying its
 * statement's header, totals and ageing cells alongside — the shape the
 * legacy Crystal dataset had (every row repeated Opening, Jan..Dec and the
 * customer block), so the template groups on {@code customerId} and reads
 * everything else off the current row.
 *
 * <p>Twelve named ageing cells rather than a list: Jasper's bean data source
 * resolves plain property names reliably; indexed paths into a list do not
 * survive a template edit as well.
 */
@Getter
@Builder
public class StatementPrintRow {

    /** The printed page shows 12/8/2026 — day and month without zero padding. */
    private static final DateTimeFormatter STATEMENT_DATE = DateTimeFormatter.ofPattern("d/M/yyyy");
    /** Lines show 08-07-2026. */
    private static final DateTimeFormatter LINE_DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final Integer customerId;
    private final String customerName;
    private final String address1;
    private final String address2;
    private final String address3;
    private final String phone;
    private final String attn;
    private final String accountCode;
    private final String terms;
    private final String currency;
    private final String statementDate;

    private final String lineDate;
    private final String documentNo;
    private final String reference;
    private final String description;
    private final BigDecimal debit;
    private final BigDecimal credit;
    private final BigDecimal balance;
    private final BigDecimal openingBalance;
    private final BigDecimal closingBalance;
    /** Lines on this customer's statement, so the template can tell a continuation page from a footer-only one. */
    private final Integer lineCount;

    private final BigDecimal totalDebit;
    private final BigDecimal totalCredit;

    private final String ageLabel1;
    private final String ageLabel2;
    private final String ageLabel3;
    private final String ageLabel4;
    private final String ageLabel5;
    private final String ageLabel6;
    private final String ageLabel7;
    private final String ageLabel8;
    private final String ageLabel9;
    private final String ageLabel10;
    private final String ageLabel11;
    private final String ageLabel12;
    private final BigDecimal ageAmount1;
    private final BigDecimal ageAmount2;
    private final BigDecimal ageAmount3;
    private final BigDecimal ageAmount4;
    private final BigDecimal ageAmount5;
    private final BigDecimal ageAmount6;
    private final BigDecimal ageAmount7;
    private final BigDecimal ageAmount8;
    private final BigDecimal ageAmount9;
    private final BigDecimal ageAmount10;
    private final BigDecimal ageAmount11;
    private final BigDecimal ageAmount12;

    /** Every line of every statement, in print order. */
    public static List<StatementPrintRow> flatten(List<CustomerStatement> statements) {
        List<StatementPrintRow> rows = new ArrayList<>();
        for (CustomerStatement s : statements) {
            List<AgeingBucket> age = calendarSlots(s.getAgeing());
            for (StatementLine line : s.getLines()) {
                rows.add(StatementPrintRow.builder()
                        .customerId(s.getCustomerId())
                        .customerName(s.getCustomerName())
                        .address1(s.getAddress1())
                        .address2(s.getAddress2())
                        .address3(s.getAddress3())
                        .phone(s.getPhone())
                        .attn(s.getAttn())
                        .accountCode(s.getAccountCode())
                        .terms(s.getTerms())
                        .currency(s.getCurrency())
                        .statementDate(format(s.getStatementDate(), STATEMENT_DATE))
                        .lineDate(format(line.date(), LINE_DATE))
                        .documentNo(line.documentNo())
                        .reference(line.reference())
                        .description(line.description())
                        .debit(line.debit())
                        .credit(line.credit())
                        .balance(line.balance())
                        .openingBalance(s.getOpeningBalance())
                        .closingBalance(s.getClosingBalance())
                        .lineCount(s.getLines() == null ? 0 : s.getLines().size())
                        .totalDebit(s.getTotalDebit())
                        .totalCredit(s.getTotalCredit())
                        .ageLabel1(label(age, 0)).ageAmount1(amount(age, 0))
                        .ageLabel2(label(age, 1)).ageAmount2(amount(age, 1))
                        .ageLabel3(label(age, 2)).ageAmount3(amount(age, 2))
                        .ageLabel4(label(age, 3)).ageAmount4(amount(age, 3))
                        .ageLabel5(label(age, 4)).ageAmount5(amount(age, 4))
                        .ageLabel6(label(age, 5)).ageAmount6(amount(age, 5))
                        .ageLabel7(label(age, 6)).ageAmount7(amount(age, 6))
                        .ageLabel8(label(age, 7)).ageAmount8(amount(age, 7))
                        .ageLabel9(label(age, 8)).ageAmount9(amount(age, 8))
                        .ageLabel10(label(age, 9)).ageAmount10(amount(age, 9))
                        .ageLabel11(label(age, 10)).ageAmount11(amount(age, 10))
                        .ageLabel12(label(age, 11)).ageAmount12(amount(age, 11))
                        .build());
            }
        }
        return rows;
    }

    /**
     * The Crystal grid was Jan..Jun over Jul..Dec - twelve fixed calendar
     * slots, each holding that month of the rolling year. Same layout here:
     * the bucket for month M sits in slot M, so "Sep" is always the bottom
     * row's third cell. The label keeps the year ("Sep 26") because a rolling
     * year has two of some months' names; the newer legacy query already
     * formatted {@code 'MMM yy'}.
     */
    static List<AgeingBucket> calendarSlots(List<AgeingBucket> rolling) {
        AgeingBucket[] slots = new AgeingBucket[12];
        if (rolling != null) {
            for (AgeingBucket b : rolling) {
                if (b.month() >= 1 && b.month() <= 12) {
                    slots[b.month() - 1] = b;
                }
            }
        }
        List<AgeingBucket> out = new ArrayList<>(12);
        for (AgeingBucket b : slots) {
            out.add(b);
        }
        return out;
    }

    /** The bare month name, "Jan".."Dec" - what the Crystal grid printed; the slot position fixes the month. */
    private static String label(List<AgeingBucket> age, int i) {
        return age != null && i < age.size() && age.get(i) != null ? age.get(i).monthName() : "";
    }

    private static BigDecimal amount(List<AgeingBucket> age, int i) {
        return age != null && i < age.size() && age.get(i) != null ? age.get(i).amount() : BigDecimal.ZERO;
    }

    private static String format(LocalDate date, DateTimeFormatter formatter) {
        return date == null ? "" : date.format(formatter);
    }
}
