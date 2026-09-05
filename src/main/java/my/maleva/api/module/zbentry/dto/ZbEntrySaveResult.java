package my.maleva.api.module.zbentry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * What {@code SP_ZBEntryMaster} reports back.
 *
 * <p>The procedure ends with {@code SELECT @Result, @msg, @Id}, and those three
 * values are the whole contract: {@code result} is 1 on success and 0 when the
 * procedure's CATCH block rolled the transaction back, {@code msg} carries the
 * SQL error text in that case, and {@code id} is the row's identity.
 *
 * <p><b>{@code id} is the id of the <i>last</i> row the procedure processed</b>,
 * not a list. The procedure loops the JSON and overwrites {@code @Id} each pass,
 * so a bulk save reports only the final row. That is fine for the entry screen,
 * which saves exactly one row and needs its new id to file attachments against —
 * but it is a trap for any future caller that saves several at once.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZbEntrySaveResult {

    /** 1 when the procedure committed, 0 when it rolled back. */
    private Integer result;

    /** "Saved Successfully", or the SQL error message on failure. */
    private String msg;

    /** Identity of the last row inserted or updated; 0 on failure. */
    private Integer id;

    public boolean isSuccess() {
        return result != null && result == 1;
    }
}
