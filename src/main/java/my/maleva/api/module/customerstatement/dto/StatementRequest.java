package my.maleva.api.module.customerstatement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

/**
 * What the Customer Statement screen asks for — the port of the legacy
 * {@code CustomerReceiptReportModel} fields that
 * {@code SelectCustomerStatementAllReport} actually read.
 *
 * <p>Legacy also sent {@code Year}, {@code ReportType} and {@code SendId}. The
 * first was computed and never used; the second was accepted and ignored by
 * this query; the third belongs to the send endpoint, not the query.
 */
@Data
public class StatementRequest {

    @NotNull(message = "Company is required")
    @Positive(message = "Company must be greater than zero")
    private Integer companyId;

    /** One customer, or null/0 for every customer with something outstanding. */
    private Integer customerId;

    /** Legacy "Include Credit Note" ({@code AccountId = 1}). */
    private boolean includeCreditNotes;

    /**
     * Legacy "Include Date" ({@code chkDateId = 1}): take invoices dated inside
     * {@code fromDate..toDate} instead of everything since the company's
     * statement cutoff.
     */
    private boolean useDateRange;

    private LocalDate fromDate;
    private LocalDate toDate;

    public boolean isSingleCustomer() {
        return customerId != null && customerId > 0;
    }
}
