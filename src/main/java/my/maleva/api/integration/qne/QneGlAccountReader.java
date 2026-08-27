package my.maleva.api.integration.qne;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Reads chart-of-accounts rows straight from QNE's database.
 *
 * <p>Port of the one live use of the legacy direct-QNE-DB connection
 * ({@code Dapperr.GetAll1} in {@code AccountsGroupMasterServices.InsertGLAccounts}).
 * The QNE REST API has no GLAccounts endpoint, so the import has to read
 * their table. Column list is exactly the legacy SELECT — the local
 * {@code SP_GLAccounts} parses the serialised rows by these property names.
 *
 * <p>The account code is a bound parameter; the legacy concatenated it into
 * the SQL and stripped apostrophes app-wide to compensate.
 *
 * <p>This bean exists only when the QNE datasource is configured
 * ({@code qne.datasource.host} non-blank); callers probe for it with an
 * {@code ObjectProvider} and report a clear error when it is absent.
 */
@Slf4j
@Component
@Lazy
@ConditionalOnExpression("!'${qne.datasource.host:}'.isEmpty()")
public class QneGlAccountReader {

    /** Legacy SELECT column list, verbatim — the SP payload contract. */
    private static final String QUERY = """
            SELECT [Id],[ParentId],[GLAccountCode],[AccountId],[SpecialAccountId],
                   [CurrencyId],[GSTTypeId],[Description],[DRCR],[IsCreditCard],
                   [IsActive],[GSTGroup],[IsRevaluation],[Notes],[IsSubAccount],
                   [BankAccountNo],[GSTMSICCode],[OptimisticLockField],[SAC],
                   [SSTTariffCode],[RowIndex],[HasChildInCoa],
                   [IncludeInCashFlowForecastAdvisor],[TariffCodeId],[ATCCodeId],
                   [Description2]
              FROM GLAccounts WITH (NOLOCK)
             WHERE IsActive = 1 AND GLAccountCode = ?
            """;

    /** The same projection with no code filter — the whole active chart. */
    private static final String ALL_QUERY =
            QUERY.substring(0, QUERY.indexOf("AND GLAccountCode = ?"));

    private final JdbcTemplate qneJdbcTemplate;

    public QneGlAccountReader(@Qualifier("qneJdbcTemplate") JdbcTemplate qneJdbcTemplate) {
        this.qneJdbcTemplate = qneJdbcTemplate;
    }

    /**
     * Every active account in QNE's chart.
     *
     * <p>Legacy reached this by leaving the account code blank: it set an
     * error message, then ran the query anyway without the code filter and
     * imported the lot. The blank case is a real feature — a first-time import
     * has no code to name — so it is spelled out here rather than inherited by
     * accident.
     */
    public List<Map<String, Object>> findAll() {
        List<Map<String, Object>> rows = qneJdbcTemplate.queryForList(ALL_QUERY);
        log.info("Read {} GL account row(s) from QNE", rows.size());
        return rows.stream().map(QneGlAccountReader::legacyShape).collect(Collectors.toList());
    }

    /**
     * The active GL accounts under one code, as ordered column-name → value
     * maps ready to serialise for {@code SP_GLAccounts}.
     */
    public List<Map<String, Object>> findByAccountCode(String accountCode) {
        List<Map<String, Object>> rows = qneJdbcTemplate.queryForList(QUERY, accountCode);
        return rows.stream().map(QneGlAccountReader::legacyShape).collect(Collectors.toList());
    }

    /**
     * Post-processing for the SP payload.
     *
     * <p>Apostrophes are stripped from text, replicating the legacy
     * {@code Replace("'", "")} so re-imported rows stay byte-identical to the
     * 2,502 already stored (none of which carry one).
     *
     * <p>Nulls stay <b>null</b>, deliberately not the legacy-looking {@code ""}:
     * OPENJSON converts {@code ''} to a {@code uniqueidentifier} by throwing
     * ("Conversion failed…", verified against the live server), while a JSON
     * null lands as SQL NULL for every column type. Legacy never hit this
     * because its C# model used non-nullable {@code Guid}s — the
     * {@code Replace("null", "\"\"")} only ever touched varchar fields. Mapping
     * every null to {@code ""} here would crash the import on the first QNE
     * account with a NULL guid column (ATCCodeId is routinely null).
     */
    private static Map<String, Object> legacyShape(Map<String, Object> row) {
        Map<String, Object> shaped = new LinkedHashMap<>();
        row.forEach((column, value) -> {
            if (value instanceof String text) {
                shaped.put(column, text.replace("'", ""));
            } else {
                shaped.put(column, value);
            }
        });
        return shaped;
    }
}
