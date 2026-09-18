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

    /**
     * CC as the operator left it, comma or semicolon separated. {@code null}
     * keeps the configured receivables CC; an empty string sends without CC.
     */
    private String cc;

    /** The subject as edited on the preview; blank uses the wording's own subject. */
    private String subject;

    /**
     * The HTML body as edited on the preview (it starts from
     * {@code /mail-preview}); blank uses the wording's own template.
     */
    private String body;

    /**
     * What to attach: {@code PDF} (the default, also for null or anything
     * unknown), {@code EXCEL} or {@code BOTH}.
     */
    private String attach;

    public boolean attachPdf() {
        return StatementAttach.of(attach).pdf();
    }

    public boolean attachExcel() {
        return StatementAttach.of(attach).excel();
    }
}
