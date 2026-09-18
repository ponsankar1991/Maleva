package my.maleva.api.module.paymentrecept.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Hands out receipt numbers (RC000000121), one caller at a time.
 *
 * <p>The old save read {@code MAX(SequenceNo)} through JPA, added one in Java
 * and saved the counter back. Nothing locked the row between the read and the
 * write, so two clerks saving a receipt at the same moment both read the same
 * value and both receipts got the same number. A missing counter row also
 * restarted at 1 and repeated numbers already issued.
 *
 * <p>{@code UPDATE ... OUTPUT INSERTED.SequenceNo} increments and reads in one
 * statement, so no two callers see the same value - the shape the invoice and
 * sale order saves use. The row stays locked until the save commits: a rolled
 * back save gives its number back, and receipt creates queue behind one another
 * for the length of a save.
 *
 * <p>The value is read from the OUTPUT rows, never from an affected-row count:
 * this datasource runs {@code SET NOCOUNT ON} and every UPDATE reports -1.
 */
@Component
public class ReceiptNumberAllocator {

    private static final Logger logger = LoggerFactory.getLogger(ReceiptNumberAllocator.class);

    /** The SequenceNoMaster row the legacy SP_Receipt counted with. */
    static final String SEQUENCE_NAME = "Receipt";

    static final String INCREMENT_SQL =
            "UPDATE SequenceNoMaster SET SequenceNo = ISNULL(SequenceNo, 0) + 1, SequenceDate = GETDATE() "
                    + "OUTPUT INSERTED.SequenceNo "
                    + "WHERE CompanyRefId = :comid AND SequenceName = :name";

    /**
     * Creates the counter row when it is missing, in one statement, seeded from
     * the highest receipt number already issued rather than 0.
     * {@code UPDLOCK, HOLDLOCK} on the existence check stops two first-ever saves
     * both inserting a row.
     */
    static final String SEED_SQL =
            "INSERT INTO SequenceNoMaster (CompanyRefId, SequenceName, SequenceNo, SequenceDate) "
                    + "SELECT :comid, :name, "
                    + "ISNULL((SELECT MAX(CNumber) FROM Receipt WHERE CompanyRefId = :comid), 0), GETDATE() "
                    + "WHERE NOT EXISTS (SELECT 1 FROM SequenceNoMaster WITH (UPDLOCK, HOLDLOCK) "
                    + "WHERE CompanyRefId = :comid AND SequenceName = :name)";

    private final NamedParameterJdbcTemplate jdbc;

    public ReceiptNumberAllocator(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Takes the next receipt number. Must run inside the save transaction. */
    public int next(Integer companyId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("comid", companyId)
                .addValue("name", SEQUENCE_NAME);

        Integer allocated = increment(params, companyId);
        if (allocated != null) {
            return allocated;
        }

        jdbc.update(SEED_SQL, params);
        logger.info("Created the receipt number counter for company {}", companyId);

        allocated = increment(params, companyId);
        if (allocated == null) {
            throw new IllegalStateException("No receipt number counter for company " + companyId);
        }
        return allocated;
    }

    /** RC + nine digits, as SP_Receipt formatted it. */
    public static String display(int number) {
        return "RC" + String.format("%09d", number);
    }

    private Integer increment(MapSqlParameterSource params, Integer companyId) {
        List<Integer> values = jdbc.queryForList(INCREMENT_SQL, params, Integer.class);
        if (values.size() > 1) {
            // Duplicate counter rows: every one was incremented. Take the highest
            // so the number is still past everything already issued.
            logger.warn("{} receipt counter rows for company {}; using the highest value", values.size(), companyId);
        }
        return values.stream().filter(Objects::nonNull).max(Integer::compare).orElse(null);
    }
}
