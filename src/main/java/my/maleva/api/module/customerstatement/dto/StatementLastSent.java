package my.maleva.api.module.customerstatement.dto;

import java.time.LocalDateTime;

/**
 * When a customer last received a statement mail, from the send log — the
 * screen's "Last sent" column, which is what stops a customer being mailed
 * twice in one round.
 *
 * @param reminder the wording that went ("" = Statement of Account)
 * @param sentTo   the addresses it went to, comma separated
 */
public record StatementLastSent(
        int customerId,
        LocalDateTime sentAt,
        String reminder,
        String sentTo,
        String sentBy
) {
}
