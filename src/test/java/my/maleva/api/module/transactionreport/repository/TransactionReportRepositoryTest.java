package my.maleva.api.module.transactionreport.repository;

import my.maleva.api.module.transactionreport.dto.PaymentDoneRowDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The SQL this repository assembles, captured before it reaches the driver.
 *
 * The query is built by concatenating two text blocks with filter fragments
 * between them, and the failures that pattern produces — a clause welded onto
 * the next keyword, a filter landing on the wrong arm of the union, a value
 * inlined instead of bound — are all invisible to the compiler and to a
 * unit test that only mocks the return value. So the statement itself is the
 * thing under test here.
 */
@ExtendWith(MockitoExtension.class)
class TransactionReportRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate jdbc;

    @InjectMocks
    private TransactionReportRepository repository;

    @SuppressWarnings("unchecked")
    private String captureRowSql() {
        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(), any(SqlParameterSource.class), any(RowMapper.class));
        return sql.getValue();
    }

    @SuppressWarnings("unchecked")
    private void stubQuery() {
        when(jdbc.query(anyString(), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.<PaymentDoneRowDto>of());
    }

    @Test
    void keepsTheUnionAndTheOrderBySeparatedFromTheClausesAroundThem() {
        stubQuery();

        repository.findCompletedPayments(6, "2026-08-01 00:00:00", "2026-08-28 23:59:59",
                0, "", List.of());

        String sql = captureRowSql();

        // A missing separator here is the classic text-block concatenation bug:
        // `...:toDate UNION SELECT` is fine, `...:toDateUNION` is not.
        assertTrue(sql.contains(":toDate\nUNION\n") || sql.contains(":toDate \nUNION"),
                () -> "arms are not separated:\n" + sql);
        assertTrue(sql.trim().endsWith("ORDER BY CNumberDisplay"),
                () -> "ORDER BY is not last:\n" + sql);
        // Both registers are present, and each keeps its own literal.
        assertTrue(sql.contains("FROM PaymentVoucherMaster PM"));
        assertTrue(sql.contains("FROM Payment P"));
        assertTrue(sql.contains("0 AS DetailedId"));
        assertTrue(sql.contains("1 AS DetailedId"));
    }

    @Test
    void putsTheSupplierFilterOnThePaymentArmAndSuppressesTheVoucherArm() {
        stubQuery();

        repository.findCompletedPayments(6, "f", "t", 42, "", List.of());

        String sql = captureRowSql();

        // A supplier cannot describe a voucher, so the voucher arm is not
        // narrowed by supplier — it is emptied. Narrowing it instead would
        // answer a supplier question with voucher rows.
        assertTrue(sql.contains("AND P.SupplierRefId = :supplierId"));
        assertTrue(sql.contains("AND PM.PayTo = ''"));
        assertFalse(sql.contains(":payTo"));
        // Bound, not inlined. The .NET original concatenated the id.
        assertFalse(sql.contains("42"));
    }

    @Test
    void putsThePayToFilterOnTheVoucherArmAndSuppressesThePaymentArm() {
        stubQuery();

        repository.findCompletedPayments(6, "f", "t", 0, "O'BRIEN SDN BHD", List.of());

        String sql = captureRowSql();

        assertTrue(sql.contains("AND PM.PayTo = :payTo"));
        assertTrue(sql.contains("AND P.SupplierRefId = 0"));
        // The apostrophe never reaches the statement — this exact value broke
        // the legacy screen, which built the clause by string concatenation.
        assertFalse(sql.contains("O'BRIEN"));
    }

    @Test
    void ignoresPayToWhenASupplierIsAlreadyChosen() {
        stubQuery();

        repository.findCompletedPayments(6, "f", "t", 42, "SOME NAME", List.of());

        String sql = captureRowSql();

        // Legacy's if/else-if: supplier wins, and the two never combine into a
        // filter that can match nothing at all.
        assertTrue(sql.contains(":supplierId"));
        assertFalse(sql.contains(":payTo"));
    }

    @Test
    void qualifiesTheCategoryFilterOnBothArms() {
        stubQuery();

        repository.findCompletedPayments(6, "f", "t", 0, "", List.of("FUEL", "TOLL"));

        String sql = captureRowSql();

        // The .NET original emitted `(P.Description='FUEL' OR Description='TOLL')`
        // — the second onwards unqualified, ambiguous the moment Supplier grows
        // a Description column. Both arms are qualified here.
        assertTrue(sql.contains("AND PM.Description IN (:descriptions)"));
        assertTrue(sql.contains("AND P.Description IN (:descriptions)"));
        assertFalse(sql.contains("'FUEL'"));
    }

    @Test
    void sumsOverTheSameFilteredUnionItReturnsRowsFor() {
        when(jdbc.queryForObject(anyString(), any(SqlParameterSource.class), eq(BigDecimal.class)))
                .thenReturn(new BigDecimal("15500.99"));

        BigDecimal total = repository.sumCompletedPayments(6, "f", "t", 0, "", List.of("FUEL"));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForObject(sql.capture(), any(SqlParameterSource.class), eq(BigDecimal.class));

        String statement = sql.getValue();
        // Same filters, wrapped — a total computed over a different row set
        // than the grid shows is worse than no total at all.
        assertTrue(statement.startsWith("SELECT ISNULL(SUM(t.Amount), 0) FROM ("));
        assertTrue(statement.trim().endsWith(") t"));
        assertTrue(statement.contains("AND PM.Description IN (:descriptions)"));
        // No ORDER BY inside the derived table; SQL Server rejects that.
        assertFalse(statement.contains("ORDER BY"));
        assertEquals(0, new BigDecimal("15500.99").compareTo(total));
    }

    @Test
    void answersZeroRatherThanNullWhenNothingMatched() {
        when(jdbc.queryForObject(anyString(), any(SqlParameterSource.class), eq(BigDecimal.class)))
                .thenReturn(null);

        assertEquals(0, BigDecimal.ZERO.compareTo(
                repository.sumCompletedPayments(6, "f", "t", 0, "", List.of())));
    }
}
