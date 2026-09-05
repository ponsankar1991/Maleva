package my.maleva.api.module.zbentry.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.zbentry.dto.ZbEntrySaveResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Calls {@code SP_ZBEntryMaster}, the procedure that owns ZB entry writes.
 *
 * <p>The procedure takes the rows as a JSON document, unpacks them with
 * {@code OPENJSON}, and loops: a row with {@code Id = 0} is inserted, anything
 * else is updated — scoped to the company either way. It runs the whole batch in
 * one transaction and rolls back on any error, which is why the write goes
 * through it rather than through row-by-row JPA saves: the insert/update rule and
 * the atomicity live in one place, and the legacy .NET screen and this one then
 * cannot drift apart.
 *
 * <p><b>The JSON is a bind parameter here.</b> The legacy caller concatenated it
 * straight into the statement text, and paid for it twice: it had to strip every
 * apostrophe out of the data first ({@code details.Replace("'", "")}, which
 * quietly turned "O'Brien Shipping" into "OBrien Shipping"), and it ran a blind
 * {@code Replace("null", "\"\"")} across the whole document, which corrupts any
 * value that merely contains those four letters. Binding removes the need for
 * both.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ZbEntryProcedureRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Run the procedure over one JSON batch.
     *
     * <p>{@code SET NOCOUNT ON} is prepended so the procedure's per-row INSERT
     * and UPDATE counts cannot arrive ahead of the result set and hide it from
     * the driver.
     *
     * @param detailsJson a JSON array of rows shaped for the procedure's
     *                    {@code OPENJSON ... WITH} column list
     * @param comId       the company every row is written under; the procedure
     *                    uses this rather than the {@code CompanyRefId} in the
     *                    JSON, so a caller cannot write into another company
     */
    public ZbEntrySaveResult save(String detailsJson, Integer comId) {
        List<ZbEntrySaveResult> rows = jdbcTemplate.query(
                "SET NOCOUNT ON; EXEC [SP_ZBEntryMaster] ?, ?",
                (rs, rowNum) -> ZbEntrySaveResult.builder()
                        .result(rs.getInt("Result"))
                        .msg(rs.getString("Msg"))
                        .id(rs.getInt("Id"))
                        .build(),
                detailsJson, comId);

        if (rows.isEmpty()) {
            // The procedure always ends in a SELECT, on both the success and the
            // failure path, so an empty result means it never reached either.
            log.error("SP_ZBEntryMaster returned no result row for comId={}", comId);
            return ZbEntrySaveResult.builder()
                    .result(0)
                    .msg("The save procedure returned no result")
                    .id(0)
                    .build();
        }

        return rows.get(0);
    }
}
