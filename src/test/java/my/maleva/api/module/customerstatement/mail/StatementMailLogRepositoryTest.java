package my.maleva.api.module.customerstatement.mail;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The send log's two promises: its writes COMMIT (the pool hands out
 * autocommit-off connections, so a bare JdbcTemplate write is rolled back on
 * return), and it never gets in the way of a send.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatementMailLogRepositoryTest {

    /** Counts what the TransactionTemplate asked for. */
    static final class RecordingTxManager implements PlatformTransactionManager {
        int begun, committed, rolledBack;

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            begun++;
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            committed++;
        }

        @Override
        public void rollback(TransactionStatus status) {
            rolledBack++;
        }
    }

    @Mock private NamedParameterJdbcTemplate jdbc;
    @Mock private JdbcOperations ops;
    private final RecordingTxManager txManager = new RecordingTxManager();

    private StatementMailLogRepository repository(boolean autoCreate) {
        when(jdbc.getJdbcOperations()).thenReturn(ops);
        return new StatementMailLogRepository(jdbc, txManager, autoCreate);
    }

    private static StatementMailLogRepository.Entry entry() {
        return new StatementMailLogRepository.Entry(1, 17, "ACS FREIGHT", List.of("a@acs.example"),
                List.of("receivable@maleva.com.my"), "Statement of Account - ACS", "",
                LocalDate.of(2026, 9, 11), new BigDecimal("18836.45"), "SGD", "CustomerStatement_ACS.pdf",
                "SENT", null, null, "mala", LocalDateTime.of(2026, 9, 11, 12, 0),
                "STATEMENT", "<statement-1@maleva.com.my>", null, null);
    }

    @Test
    @DisplayName("startup: the DDL runs inside a committed transaction, then a probe on a fresh connection confirms the table")
    void ensureTableCommitsTheDdl() {
        StatementMailLogRepository repo = repository(true);
        when(ops.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);

        repo.ensureTable();

        verify(ops).execute(contains("CREATE TABLE dbo.CustomerStatementMailLog"));
        assertThat(txManager.begun).isEqualTo(1);
        assertThat(txManager.committed).isEqualTo(1);
        assertThat(txManager.rolledBack).isZero();
        verify(ops).queryForObject(contains("FROM dbo.CustomerStatementMailLog WHERE 1 = 0"), eq(Integer.class));
        assertThat(repo.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("auto-create off: no DDL, just the probe")
    void autoCreateOff() {
        StatementMailLogRepository repo = repository(false);
        when(ops.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);

        repo.ensureTable();

        verify(ops, never()).execute(anyString());
        assertThat(txManager.begun).isZero();
        assertThat(repo.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("a table that is not there makes the log unavailable: nothing is written, reads are empty, no exception")
    void unavailableWhenProbeFails() {
        StatementMailLogRepository repo = repository(false);
        when(ops.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new BadSqlGrammarException("probe", "SELECT", new SQLException("Invalid object name 'dbo.CustomerStatementMailLog'.")));

        repo.ensureTable();

        assertThat(repo.isAvailable()).isFalse();
        assertThat(repo.insert(entry())).isFalse();
        verify(jdbc, never()).update(anyString(), any(SqlParameterSource.class));
        assertThat(repo.findLastSent(1)).isEmpty();
    }

    @Test
    @DisplayName("an insert commits")
    void insertCommits() {
        StatementMailLogRepository repo = repository(false);
        when(ops.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
        repo.ensureTable();
        when(jdbc.update(anyString(), any(SqlParameterSource.class))).thenReturn(1);

        assertThat(repo.insert(entry())).isTrue();

        verify(jdbc).update(contains("INSERT INTO dbo.CustomerStatementMailLog"), any(SqlParameterSource.class));
        assertThat(txManager.begun).isEqualTo(1);
        assertThat(txManager.committed).isEqualTo(1);
    }

    @Test
    @DisplayName("an insert that fails rolls back and answers false; it never throws at the sender")
    void insertNeverThrows() {
        StatementMailLogRepository repo = repository(false);
        when(ops.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);
        repo.ensureTable();
        when(jdbc.update(anyString(), any(SqlParameterSource.class)))
                .thenThrow(new DataIntegrityViolationException("String or binary data would be truncated"));

        assertThat(repo.insert(entry())).isFalse();

        assertThat(txManager.rolledBack).isEqualTo(1);
        assertThat(txManager.committed).isZero();
    }
}
