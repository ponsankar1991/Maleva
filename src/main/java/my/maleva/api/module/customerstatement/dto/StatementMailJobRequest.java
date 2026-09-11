package my.maleva.api.module.customerstatement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * A bulk statement run: the statement filters, the wording, and the customers
 * to mail — each with the addresses the operator saw and could edit on the
 * screen.
 *
 * <p>The server builds every statement in one pass (the same query the View
 * runs for "all customers"), renders one PDF per customer and mails them in
 * batches over one SMTP connection at a time, in the background. The screen
 * follows the run through {@code GET /mail-jobs/{id}}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StatementMailJobRequest extends StatementRequest {

    @NotEmpty(message = "Choose at least one customer to send to")
    @Size(max = 2000, message = "At most 2000 customers per run")
    @Valid
    private List<Recipient> customers;

    /** "", "Reminder 1" or "Reminder 2" — the same wording for every customer in the run. */
    private String reminder;

    @Data
    public static class Recipient {

        @NotNull(message = "Customer id is required")
        @Positive(message = "Customer id must be greater than zero")
        private Integer customerId;

        /**
         * Comma or semicolon separated. {@code null} means "use the addresses
         * on the customer master"; an empty string means the operator cleared
         * the box, and the customer is skipped rather than mailed to an address
         * they removed.
         */
        private String emails;
    }
}
