package my.maleva.api.module.customerstatement.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One printed line of a statement: DATE · INVOICE NO · Reference ·
 * DESCRIPTION · DEBIT · CREDIT · BALANCE.
 *
 * @param kind        what the line is, so the screen can style credit notes
 * @param date        the invoice or credit-note date
 * @param documentNo  CNumberDisplay of the invoice or credit note
 * @param reference   SaleMaster.Remarks1 — the "Referance" column, blank for a credit note
 * @param description off-vessel name, else loading-vessel name; for a credit
 *                    note, the invoice it was knocked off against
 * @param debit       what the customer still owes on this invoice
 * @param credit      what a credit note took off
 * @param balance     running balance down the page, from zero — the legacy
 *                    report never printed an opening balance
 */
public record StatementLine(
        Kind kind,
        LocalDate date,
        String documentNo,
        String reference,
        String description,
        BigDecimal debit,
        BigDecimal credit,
        BigDecimal balance
) {
    public enum Kind { INVOICE, CREDIT_NOTE }

    public StatementLine withBalance(BigDecimal running) {
        return new StatementLine(kind, date, documentNo, reference, description, debit, credit, running);
    }
}
