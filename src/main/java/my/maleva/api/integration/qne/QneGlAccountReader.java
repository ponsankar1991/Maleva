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

    private final JdbcTemplate qneJdbcTemplate;

    public QneGlAccountReader(@Qualifier("qneJdbcTemplate") JdbcTemplate qneJdbcTemplate) {
        this.qneJdbcTemplate = qneJdbcTemplate;
    }

    /**
     * The active GL accounts under one code, as ordered column-name → value
     * maps ready to serialise for {@code SP_GLAccounts}.
     *
     * <p>Nulls become empty strings and apostrophes are removed from text
     * values — both replicate the legacy post-processing
     * ({@code Replace("null", "\"\"")} and {@code Replace("'", "")}), so the
     * SP receives byte-identical data to what it has always received.
     */
    public List<Map<String, Object>> findByAccountCode(String accountCode) {
        List<Map<String, Object>> rows = qneJdbcTemplate.queryForList(QUERY, accountCode);
        return rows.stream().map(QneGlAccountReader::legacyShape).collect(Collectors.toList());
    }

    private static Map<String, Object> legacyShape(Map<String, Object> row) {
        Map<String, Object> shaped = new LinkedHashMap<>();
        row.forEach((column, value) -> {
            if (value == null) {
                shaped.put(column, "");
            } else if (value instanceof String text) {
                shaped.put(column, text.replace("'", ""));
            } else {
                shaped.put(column, value);
            }
        });
        return shaped;
    }
}
