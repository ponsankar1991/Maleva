package my.maleva.api.module.customerstatement.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * The answer to one statement query: every customer that has something
 * outstanding under the filters, plus the windows the numbers were computed
 * over, so the screen can say what it is showing.
 */
@Data
@Builder
public class StatementResult {

    private List<CustomerStatement> statements;

    private int customerCount;
    private int lineCount;

    /** The company's statement cutoff — invoices before it are not listed. */
    private LocalDate cutoffDate;
    /** The invoice window actually used: the cutoff onwards, or the date range asked for. */
    private LocalDate periodFrom;
    private LocalDate periodTo;
    /** The twelve-month ageing window. */
    private LocalDate ageingFrom;
    private LocalDate ageingTo;
}
