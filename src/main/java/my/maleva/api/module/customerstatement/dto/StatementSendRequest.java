package my.maleva.api.module.customerstatement.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The Send button: the statement filters plus who to mail and which wording.
 *
 * <p>Legacy sent {@code Emailids}, {@code OverDuedate}, {@code OverdueAmount},
 * {@code Reminder} and {@code SendId = 1}. The overdue figures are not taken
 * from the caller here — they were computed in the browser and wrong for any
 * run with more than one customer — the server recomputes them from the same
 * statement it attaches.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StatementSendRequest extends StatementRequest {

    /** Recipients, comma or semicolon separated, as typed in the Emails box. */
    private String emails;

    /**
     * "" / null for the plain statement mail, "Reminder 1" or "Reminder 2" for
     * the two follow-up wordings. Anything else is treated as the plain mail —
     * legacy listed Reminder 3–5 in the dropdown and had no wording for them.
     */
    private String reminder;
}
