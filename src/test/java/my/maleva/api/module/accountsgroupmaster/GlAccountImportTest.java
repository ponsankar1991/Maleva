package my.maleva.api.module.accountsgroupmaster;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QneGlAccountReader;
import my.maleva.api.module.accountsgroupmaster.mapper.AccountsGroupMasterMapper;
import my.maleva.api.module.accountsgroupmaster.mapper.ClassificationMapper;
import my.maleva.api.module.accountsgroupmaster.mapper.GLAccountMapper;
import my.maleva.api.module.accountsgroupmaster.repository.AccountsGroupMasterRepository;
import my.maleva.api.module.accountsgroupmaster.repository.ClassificationRepository;
import my.maleva.api.module.accountsgroupmaster.repository.GLAccountRepository;
import my.maleva.api.module.accountsgroupmaster.service.impl.AccountsGroupMasterServiceImpl;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.ObjectProvider;

/**
 * The chart-of-accounts import, end to end at the service seam.
 *
 * <p>Exists because the previous migration of {@code insertGLAccounts}
 * queried the local table — where the account cannot yet exist — and inserted
 * nothing, reporting success. These tests pin the real flow: rows come from
 * the QNE reader, go to {@code SP_GLAccounts} as one JSON parameter, and the
 * SP's own verdict decides the response.
 */
@ExtendWith(MockitoExtension.class)
class GlAccountImportTest {

    @Mock private AccountsGroupMasterRepository accountsRepo;
    @Mock private GLAccountRepository glRepo;
    @Mock private ClassificationRepository classificationRepo;
    @Mock private AccountsGroupMasterMapper accountsMapper;
    @Mock private GLAccountMapper glMapper;
    @Mock private ClassificationMapper classificationMapper;
    @Mock private ObjectProvider<QneGlAccountReader> readerProvider;
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private QneGlAccountReader reader;

    private AccountsGroupMasterServiceImpl service() {
        return new AccountsGroupMasterServiceImpl(
                accountsRepo, glRepo, classificationRepo,
                accountsMapper, glMapper, classificationMapper,
                readerProvider, jdbcTemplate, new ObjectMapper());
    }

    @Test
    void importsQneRowsThroughTheStoredProcedure() {
        when(readerProvider.getIfAvailable()).thenReturn(reader);
        // LinkedHashMap, as the reader returns: the SP payload is ordered.
        Map<String, Object> row = new java.util.LinkedHashMap<>();
        row.put("GLAccountCode", "6000-001");
        row.put("Description", "FUEL");
        when(reader.findByAccountCode("6000-001")).thenReturn(List.of(row));
        when(jdbcTemplate.queryForMap(anyString(), any(Object[].class)))
                .thenReturn(Map.of("Result", 1, "Msg", "ok"));

        ApiResponse<Void> response = service().insertGLAccounts(6, "6000-001");

        assertThat(response.isSuccess()).isTrue();
        // The SP receives the serialised rows and the company id, as parameters.
        verify(jdbcTemplate).queryForMap(
                eq("EXEC [SP_GLAccounts] ?, ?"),
                eq("[{\"GLAccountCode\":\"6000-001\",\"Description\":\"FUEL\"}]"),
                eq(6));
    }

    @Test
    void surfacesTheStoredProceduresOwnRejection() {
        when(readerProvider.getIfAvailable()).thenReturn(reader);
        when(reader.findByAccountCode("6000-001"))
                .thenReturn(List.of(Map.of("GLAccountCode", "6000-001")));
        when(jdbcTemplate.queryForMap(anyString(), any(Object[].class)))
                .thenReturn(Map.of("Result", 0, "Msg", "Account already mapped"));

        ApiResponse<Void> response = service().insertGLAccounts(6, "6000-001");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Account already mapped");
    }

    @Test
    void reportsWhenTheAccountIsNotInQne() {
        when(readerProvider.getIfAvailable()).thenReturn(reader);
        when(reader.findByAccountCode("9999")).thenReturn(List.of());

        ApiResponse<Void> response = service().insertGLAccounts(6, "9999");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("not found in QNE");
    }

    /** An environment without QNE DB access degrades to a clear message, not a crash. */
    @Test
    void reportsWhenQneDatabaseAccessIsNotConfigured() {
        when(readerProvider.getIfAvailable()).thenReturn(null);

        ApiResponse<Void> response = service().insertGLAccounts(6, "6000-001");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("not configured");
    }

    @Test
    void rejectsABlankAccountCodeBeforeTouchingAnything() {
        ApiResponse<Void> response = service().insertGLAccounts(6, "  ");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Account Code");
    }
}
