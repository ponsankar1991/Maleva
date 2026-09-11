package my.maleva.api.module.customerstatement.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * One customer's Statement of Account, fully computed — every number the
 * printed page shows is a field here, so the PDF template and the screen
 * both just render.
 *
 * <p>The header aliases are the legacy report's, spelled honestly:
 * {@code attn} is {@code Customer.City} (the PIC name column),
 * {@code phone} is {@code OPhone} (legacy called it Address2),
 * {@code accountCode} is {@code CompanyCode} (the QNE code).
 */
@Data
@Builder
public class CustomerStatement {

    private Integer customerId;
    private String customerName;
    private String address1;
    private String address2;
    private String address3;
    /** Printed under the address; legacy aliased OPhone as "Address2". */
    private String phone;
    /** "ACCOUNTS DEPT" on the sample — Customer.City, the person in charge. */
    private String attn;
    /** A/C CODE on the page: the QNE company code. */
    private String accountCode;
    private String terms;
    /** Currency symbol name (SGD, MYR…). Amounts are in it; nothing is converted. */
    private String currency;
    /** AEmail, AEmail1, OEmail, OEmail1 — the non-blank ones, in that order. */
    private List<String> emails;

    private LocalDate statementDate;

    private List<StatementLine> lines;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    /** Σdebit − Σcredit: what the customer owes on this statement. */
    private BigDecimal closingBalance;

    /** Twelve rolling months ending in the current month, oldest first. */
    private List<AgeingBucket> ageing;

    /**
     * {@code CustomerBalance(company, cutoff − 1 day)}. Legacy computed it for
     * every statement and printed it nowhere; carried so a template can choose
     * to show it. Note the function counts every sale type and ignores
     * {@code Active}, so it need not reconcile with the lines above.
     */
    private BigDecimal openingBalance;

    /** For the reminder mail: the closing balance, as legacy meant it. */
    private BigDecimal overdueAmount;
    /** For the reminder mail: the latest line date. */
    private LocalDate overdueAsOf;
}
