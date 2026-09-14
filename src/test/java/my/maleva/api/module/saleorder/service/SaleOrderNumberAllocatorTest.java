package my.maleva.api.module.saleorder.service;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Job numbers come from one atomic statement, and a missing counter starts past
 * the numbers already issued instead of at 1.
 */
class SaleOrderNumberAllocatorTest {

    private final NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
    private final SaleOrderNumberAllocator allocator = new SaleOrderNumberAllocator(jdbc);

    @Test
    void theNumberIsIncrementedAndReadInOneStatement() {
        when(jdbc.queryForList(eq(SaleOrderNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class)))
                .thenReturn(List.of(2605172));

        assertThat(allocator.next(6, "SaleOrderMasterMY", "MY")).isEqualTo(2605172);

        assertThat(SaleOrderNumberAllocator.INCREMENT_SQL)
                .contains("SET SequenceNo = ISNULL(SequenceNo, 0) + 1")
                .contains("OUTPUT INSERTED.SequenceNo");
        verify(jdbc, never()).update(eq(SaleOrderNumberAllocator.SEED_SQL), any(SqlParameterSource.class));
    }

    @Test
    void aMissingCounterIsSeededFromTheHighestIssuedNumberThenIncremented() {
        when(jdbc.queryForList(eq(SaleOrderNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class)))
                .thenReturn(List.of())
                .thenReturn(List.of(318));

        assertThat(allocator.next(6, "SaleOrderMasterTR", "TR")).isEqualTo(318);

        InOrder order = inOrder(jdbc);
        order.verify(jdbc).queryForList(eq(SaleOrderNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class));
        order.verify(jdbc).update(eq(SaleOrderNumberAllocator.SEED_SQL), any(SqlParameterSource.class));
        order.verify(jdbc).queryForList(eq(SaleOrderNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class));
        assertThat(SaleOrderNumberAllocator.SEED_SQL)
                .contains("MAX(CNumber) FROM SaleOrderMaster")
                .contains("WITH (UPDLOCK, HOLDLOCK)");
    }

    @Test
    void duplicateCounterRowsYieldTheHighestValue() {
        when(jdbc.queryForList(eq(SaleOrderNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class)))
                .thenReturn(List.of(40, 57));

        assertThat(allocator.next(6, "SaleOrderMasterMY", "MY")).isEqualTo(57);
    }

    @Test
    void noCounterEvenAfterSeedingIsRefusedRatherThanNumberedOne() {
        when(jdbc.queryForList(eq(SaleOrderNumberAllocator.INCREMENT_SQL), any(SqlParameterSource.class), eq(Integer.class)))
                .thenReturn(List.of());

        assertThatThrownBy(() -> allocator.next(6, "SaleOrderMasterMY", "MY"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SaleOrderMasterMY");
    }
}
