package my.maleva.api.module.paymentrecept.service;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Receipt numbers come from one atomic statement, and a missing counter starts
 * past the receipts already issued instead of at 1.
 */
class ReceiptNumberAllocatorTest {

    private final NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
    private final ReceiptNumberAllocator allocator = new ReceiptNumberAllocator(jdbc);

    @Test
    void theNumberIsIncrementedAndReadInOneStatementOnTheReceiptCounter() {
        when(jdbc.queryForList(eq(ReceiptNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class)))
                .thenReturn(List.of(121));

        assertThat(allocator.next(6)).isEqualTo(121);

        assertThat(ReceiptNumberAllocator.INCREMENT_SQL)
                .contains("SET SequenceNo = ISNULL(SequenceNo, 0) + 1")
                .contains("OUTPUT INSERTED.SequenceNo");
        verify(jdbc).queryForList(eq(ReceiptNumberAllocator.INCREMENT_SQL),
                argThat((SqlParameterSource p) -> Integer.valueOf(6).equals(p.getValue("comid"))
                        && "Receipt".equals(p.getValue("name"))),
                eq(Integer.class));
        verify(jdbc, never()).update(eq(ReceiptNumberAllocator.SEED_SQL), any(SqlParameterSource.class));
    }

    @Test
    void aMissingCounterIsSeededFromTheHighestIssuedReceiptThenIncremented() {
        when(jdbc.queryForList(eq(ReceiptNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class)))
                .thenReturn(List.of())
                .thenReturn(List.of(4312));

        assertThat(allocator.next(6)).isEqualTo(4312);

        InOrder order = inOrder(jdbc);
        order.verify(jdbc).queryForList(eq(ReceiptNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class));
        order.verify(jdbc).update(eq(ReceiptNumberAllocator.SEED_SQL), any(SqlParameterSource.class));
        order.verify(jdbc).queryForList(eq(ReceiptNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class));
        assertThat(ReceiptNumberAllocator.SEED_SQL)
                .contains("MAX(CNumber) FROM Receipt WHERE CompanyRefId = :comid")
                .contains("WITH (UPDLOCK, HOLDLOCK)");
    }

    @Test
    void duplicateCounterRowsTakeTheHighestValue() {
        when(jdbc.queryForList(eq(ReceiptNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class)))
                .thenReturn(List.of(88, 121));

        assertThat(allocator.next(6)).isEqualTo(121);
    }

    @Test
    void noCounterEvenAfterSeedingFailsTheSave() {
        when(jdbc.queryForList(eq(ReceiptNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> allocator.next(6)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void theDisplayNumberKeepsTheLegacyFormat() {
        assertThat(ReceiptNumberAllocator.display(121)).isEqualTo("RC000000121");
    }
}
