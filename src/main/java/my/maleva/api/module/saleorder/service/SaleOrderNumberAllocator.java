package my.maleva.api.module.saleorder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Hands out sale order job numbers, one caller at a time.
 *
 * <p>The old code read the SequenceNoMaster row through JPA, added one in Java
 * and saved it back. Nothing locked the row between the read and the write and
 * the table has no version column, so two people saving a new order of the same
 * bill type at the same moment both read the same value and both orders got the
 * same job number.
 *
 * <p>{@code UPDATE ... OUTPUT INSERTED.SequenceNo} increments and reads in one
 * statement, so no two callers can see the same value — the same shape the
 * invoice save uses. The row stays locked until the save commits, so a rolled
 * back save gives its number back and the sequence has no gap. The cost is that
 * creates of the <em>same bill type</em> queue behind one another for the length
 * of a save; different bill types use different rows and never wait.
 *
 * <p>Counts are read from the OUTPUT rows, never from an affected-row count,
 * because this datasource runs {@code SET NOCOUNT ON} and every UPDATE reports -1.
 */
@Component
public class SaleOrderNumberAllocator {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderNumberAllocator.class);

    static final String INCREMENT_SQL =
            "UPDATE SequenceNoMaster SET SequenceNo = ISNULL(SequenceNo, 0) + 1, SequenceDate = GETDATE() "
                    + "OUTPUT INSERTED.SequenceNo "
                    + "WHERE CompanyRefId = :comid AND SequenceName = :name";

    /**
     * Creates the counter row when it is missing, in one statement.
     *
     * <p>Seeded from the highest job number already issued for that bill type,
     * not from 0: the old fallback started a missing counter at 1 and the next
     * order repeated a job number that already existed. {@code UPDLOCK, HOLDLOCK}
     * on the existence check means two first-ever saves cannot both insert a row.
     */
    static final String SEED_SQL =
            "INSERT INTO SequenceNoMaster (CompanyRefId, SequenceName, SequenceNo, SequenceDate) "
                    + "SELECT :comid, :name, "
                    + "ISNULL((SELECT MAX(CNumber) FROM SaleOrderMaster "
                    + "WHERE CompanyRefId = :comid AND LTRIM(RTRIM(BillType)) = :billType), 0), GETDATE() "
                    + "WHERE NOT EXISTS (SELECT 1 FROM SequenceNoMaster WITH (UPDLOCK, HOLDLOCK) "
                    + "WHERE CompanyRefId = :comid AND SequenceName = :name)";

    private final NamedParameterJdbcTemplate jdbc;

    public SaleOrderNumberAllocator(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Takes the next number. Must run inside the save transaction.
     *
     * @param sequenceName the counter row's name, e.g. {@code SaleOrderMasterMY}
     * @param billType     the trimmed bill type, used only to seed a missing row
     * @throws IllegalStateException when no counter row exists even after seeding
     */
    public int next(Integer companyId, String sequenceName, String billType) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("comid", companyId)
                .addValue("name", sequenceName)
                .addValue("billType", billType);

        Integer allocated = increment(params, sequenceName, companyId);
        if (allocated != null) {
            return allocated;
        }

        jdbc.update(SEED_SQL, params);
        logger.info("Created the {} job number counter for company {}", sequenceName, companyId);

        allocated = increment(params, sequenceName, companyId);
        if (allocated == null) {
            throw new IllegalStateException("No job number counter " + sequenceName + " for company " + companyId);
        }
        return allocated;
    }

    private Integer increment(MapSqlParameterSource params, String sequenceName, Integer companyId) {
        List<Integer> values = jdbc.queryForList(INCREMENT_SQL, params, Integer.class);
        if (values.size() > 1) {
            // Duplicate counter rows: every one was incremented. Take the highest
            // so the number is still past everything already issued.
            logger.warn("{} counter rows named {} for company {}; using the highest value",
                    values.size(), sequenceName, companyId);
        }
        return values.stream().filter(Objects::nonNull).max(Integer::compare).orElse(null);
    }
}
