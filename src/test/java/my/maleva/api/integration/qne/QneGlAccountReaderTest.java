package my.maleva.api.integration.qne;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
 * Pins the legacy payload shaping for {@code SP_GLAccounts}: the SP has only
 * ever seen rows where SQL NULL arrives as an empty string and apostrophes
 * are stripped, because the legacy post-processed its serialised JSON that
 * way. Feeding it anything else is an untested input.
 */
class QneGlAccountReaderTest {

    @Test
    void shapesRowsTheWayTheLegacySpExpects() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Id", "a3f0");
        row.put("GLAccountCode", "6000-001");
        row.put("Description", "O'BRIEN & SONS EXPENSES");
        row.put("Notes", null);
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
        // SQL NULL arrives as "", matching the legacy Replace("null", "\"\"").
        assertThat(out.get("Notes")).isEqualTo("");
        // Non-strings pass through untouched.
        assertThat(out.get("RowIndex")).isEqualTo(7);
        assertThat(out.get("IsActive")).isEqualTo(Boolean.TRUE);
        // Column order is preserved — the SP payload has always been ordered.
        assertThat(out.keySet()).containsExactly(
                "Id", "GLAccountCode", "Description", "Notes", "RowIndex", "IsActive");
    }
}
