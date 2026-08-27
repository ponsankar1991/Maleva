package my.maleva.api.integration.qne;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pins how QNE rows are shaped before they are written locally.
 *
 * <p>Apostrophes are stripped, replicating the legacy {@code Replace("'", "")}
 * so a re-imported row stays byte-identical to what is already stored.
 *
 * <p><b>Nulls stay null.</b> The legacy also ran
 * {@code Replace("null", "\"\"")} on its serialised JSON, and an earlier
 * version of this test pinned that. It is wrong here: the values are now bound
 * straight into the local upsert as JDBC parameters, so an empty string would
 * be handed to a {@code uniqueidentifier} column and the insert would fail.
 * (The same holds for the OPENJSON route the procedure used — {@code ''} to
 * {@code uniqueidentifier} throws.) Legacy never hit it because its C# model
 * used non-nullable {@code Guid}s, so the replace only ever touched varchar
 * fields; QNE columns like {@code ATCCodeId} are routinely null.
 */
class QneGlAccountReaderTest {

    @Test
    void stripsApostrophesAndKeepsNullsNull() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Id", "a3f0");
        row.put("GLAccountCode", "6000-001");
        row.put("Description", "O'BRIEN & SONS EXPENSES");
        row.put("Notes", null);
        // A null guid column is the case that made "" unusable.
        row.put("ATCCodeId", null);
        row.put("RowIndex", 7);
        row.put("IsActive", Boolean.TRUE);
        List<Map<String, Object>> raw = new ArrayList<>();
        raw.add(row);

        when(jdbc.queryForList(anyString(), any(Object[].class))).thenReturn(raw);

        List<Map<String, Object>> shaped =
                new QneGlAccountReader(jdbc).findByAccountCode("6000-001");

        assertThat(shaped).hasSize(1);
        Map<String, Object> out = shaped.get(0);

        // Apostrophes stripped, matching the legacy Replace("'", "").
        assertThat(out.get("Description")).isEqualTo("OBRIEN & SONS EXPENSES");

        // SQL NULL stays null so it binds as NULL rather than an empty string.
        assertThat(out.get("Notes")).isNull();
        assertThat(out.get("ATCCodeId")).isNull();

        // Non-strings pass through untouched.
        assertThat(out.get("RowIndex")).isEqualTo(7);
        assertThat(out.get("IsActive")).isEqualTo(Boolean.TRUE);

        // Column order is preserved — the payload has always been ordered, and
        // the upsert reads columns positionally.
        assertThat(out.keySet()).containsExactly(
                "Id", "GLAccountCode", "Description", "Notes", "ATCCodeId", "RowIndex", "IsActive");
    }
}
