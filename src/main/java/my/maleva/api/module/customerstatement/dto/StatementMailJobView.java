package my.maleva.api.module.customerstatement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * A snapshot of a bulk statement run for the screen — polled while it runs.
 *
 * @param status      QUEUED, RUNNING, DONE, CANCELLED or FAILED (FAILED is the
 *                    run itself, e.g. the relay refused the login; a refused
 *                    mail is a FAILED item, not a failed run)
 * @param error       why the run stopped, when it did
 * @param total       customers in the run
 * @param sent        mails the relay accepted
 * @param failed      mails the relay refused, or that could not be rendered
 * @param skipped     customers with nothing outstanding or no address
 * @param cancelled   customers not reached before Cancel
 * @param pending     still to do
 */
public record StatementMailJobView(
        String id,
        int companyId,
        String status,
        String reminder,
        /** PDF, EXCEL or BOTH: what every customer in the run is sent. */
        String attach,
        String requestedBy,
        LocalDateTime createdAt,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        String error,
        int total,
        int sent,
        int failed,
        int skipped,
        int cancelled,
        int pending,
        List<Item> items
) {
    /**
     * One customer in the run.
     *
     * @param status PENDING, SENDING, SENT, FAILED, SKIPPED or CANCELLED
     */
    public record Item(
            int customerId,
            String customerName,
            List<String> emails,
            String status,
            String error,
            LocalDateTime sentAt,
            String subject,
            String attachmentName,
            BigDecimal overdueAmount,
            String currency
    ) {
    }
}
