package my.maleva.api.module.accountsgroupmaster;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QneGlAccountReader;
import my.maleva.api.module.accountsgroupmaster.mapper.AccountsGroupMasterMapper;
import my.maleva.api.module.accountsgroupmaster.mapper.ClassificationMapper;
import my.maleva.api.module.accountsgroupmaster.mapper.GLAccountMapper;
import my.maleva.api.module.accountsgroupmaster.repository.AccountsGroupMasterRepository;
import my.maleva.api.module.accountsgroupmaster.repository.ClassificationRepository;
import org.springframework.transaction.PlatformTransactionManager;
import my.maleva.api.module.accountsgroupmaster.repository.GLAccountRepository;
import my.maleva.api.module.accountsgroupmaster.service.impl.AccountsGroupMasterServiceImpl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.ObjectProvider;

/**
 * The chart-of-accounts import, end to end at the service seam.
 *
 * <p>Exists because the first migration of {@code insertGLAccounts} queried
 * the local table — where the account cannot yet exist — and inserted nothing,
 * reporting success. These tests pin the real flow: rows come from the QNE
 * reader and are upserted by {@code GLAccountCode} in Java
 * ({@code SP_GLAccounts} reimplemented, no longer called) — a new code is
 * INSERTed under IDENTITY_INSERT so QNE's RowIndex carries across, an existing
 * code is UPDATEd, and a blank code means the whole chart.
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
    @Mock private PlatformTransactionManager transactionManager;

    private AccountsGroupMasterServiceImpl service() {
        return new AccountsGroupMasterServiceImpl(
                accountsRepo, glRepo, classificationRepo,
                accountsMapper, glMapper, classificationMapper,
                readerProvider, jdbcTemplate, transactionManager);
    }

    /** LinkedHashMap, as the reader returns: the projection is ordered. */
    private static Map<String, Object> qneRow(String code) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("Id", "0b7d9d3e-0000-0000-0000-000000000001");
        row.put("GLAccountCode", code);
        row.put("Description", "FUEL");
        row.put("RowIndex", 42);
        return row;
    }

    @Test
    void insertsANewCodeUnderIdentityInsert() {
        when(readerProvider.getIfAvailable()).thenReturn(reader);
        when(reader.findByAccountCode("6000-001")).thenReturn(List.of(qneRow("6000-001")));
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), any(Object[].class)))
                .thenReturn(List.of()); // code not present locally

        ApiResponse<Void> response = service().insertGLAccounts(6, "6000-001");

        assertThat(response.isSuccess()).isTrue();

        // ON before the batch, OFF after — QNE's RowIndex must carry across,
        // and the setting must never leak to the pooled connection.
        ArgumentCaptor<List<Object[]>> batch = ArgumentCaptor.forClass(List.class);
        InOrder order = inOrder(jdbcTemplate);
        order.verify(jdbcTemplate).execute("SET IDENTITY_INSERT GLAccounts ON");
        order.verify(jdbcTemplate).batchUpdate(startsWith("INSERT INTO GLAccounts"), batch.capture());
        order.verify(jdbcTemplate).execute("SET IDENTITY_INSERT GLAccounts OFF");

        Object[] args = batch.getValue().get(0);
        assertThat(args[1]).isEqualTo(6);          // CompanyRefId from the request
        assertThat(args).contains("6000-001", 42); // the code and QNE's RowIndex travel
    }

    @Test
    void updatesAnExistingCodeWithoutIdentityInsert() {
        when(readerProvider.getIfAvailable()).thenReturn(reader);
        when(reader.findByAccountCode("6000-001")).thenReturn(List.of(qneRow("6000-001")));
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), any(Object[].class)))
                .thenReturn(List.of("6000-001")); // already present

        ApiResponse<Void> response = service().insertGLAccounts(6, "6000-001");

        assertThat(response.isSuccess()).isTrue();
        ArgumentCaptor<List<Object[]>> batch = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(startsWith("UPDATE GLAccounts SET"), batch.capture());
        // The match key is the last parameter; RowIndex is never in the SET list.
        Object[] args = batch.getValue().get(0);
        assertThat(args[args.length - 1]).isEqualTo("6000-001");
        verify(jdbcTemplate, never()).execute(anyString());
    }

    @Test
    void notesTravelsOnUpdate() {
        // The legacy SP's "@Notes=@Notes" self-assignment meant Notes never
        // refreshed; the Java port fixes that, and this pins the fix.
        when(readerProvider.getIfAvailable()).thenReturn(reader);
        Map<String, Object> row = qneRow("6000-001");
        row.put("Notes", "FROM QNE");
        when(reader.findByAccountCode("6000-001")).thenReturn(List.of(row));
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), any(Object[].class)))
                .thenReturn(List.of("6000-001"));

        service().insertGLAccounts(6, "6000-001");

        ArgumentCaptor<List<Object[]>> batch = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(startsWith("UPDATE GLAccounts SET"), batch.capture());
        assertThat(batch.getValue().get(0)).contains("FROM QNE");
    }

    @Test
    void identityInsertIsSwitchedOffEvenWhenTheBatchFails() {
        when(readerProvider.getIfAvailable()).thenReturn(reader);
        when(reader.findByAccountCode("6000-001")).thenReturn(List.of(qneRow("6000-001")));
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), any(Object[].class)))
                .thenReturn(List.of());
        when(jdbcTemplate.batchUpdate(startsWith("INSERT INTO GLAccounts"), anyList()))
                .thenThrow(new RuntimeException("constraint violation"));

        ApiResponse<Void> response = service().insertGLAccounts(6, "6000-001");

        assertThat(response.isSuccess()).isFalse();
        // A rollback does not reset session state — the legacy SP leaked
        // exactly this by switching IDENTITY_INSERT ON twice.
        verify(jdbcTemplate).execute("SET IDENTITY_INSERT GLAccounts OFF");
    }

    @Test
    void blankCodeImportsTheWholeChart() {
        // Legacy reached this by accident (it set an error message, then ran
        // the unfiltered query anyway); a first-time import has no code to
        // name, so blank is the full import — not a rejection.
        when(readerProvider.getIfAvailable()).thenReturn(reader);
        when(reader.findAll()).thenReturn(List.of(qneRow("1000-000"), qneRow("2000-000")));
        when(jdbcTemplate.queryForList(anyString(), eq(String.class), any(Object[].class)))
                .thenReturn(List.of());

        ApiResponse<Void> response = service().insertGLAccounts(6, "  ");

        assertThat(response.isSuccess()).isTrue();
        verify(reader).findAll();
        ArgumentCaptor<List<Object[]>> batch = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate).batchUpdate(startsWith("INSERT INTO GLAccounts"), batch.capture());
        assertThat(batch.getValue()).hasSize(2);
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
}
