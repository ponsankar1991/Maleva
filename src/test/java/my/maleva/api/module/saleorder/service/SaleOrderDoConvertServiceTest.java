package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.DoConvertResult;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class SaleOrderDoConvertServiceTest {

    private final NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
    private final SaleOrderDoConvertService service = new SaleOrderDoConvertService(jdbc);

    @Test
    void anInvoiceWithoutASaleOrderIsRefusedBeforeTouchingTheDatabase() {
        DoConvertResult result = service.convert(0, 1);

        assertThat(result.ok()).isFalse();
        assertThat(result.message()).contains("no sale order");
        verify(jdbc, never()).query(any(String.class), any(SqlParameterSource.class), any(RowMapper.class));
    }

    @Test
    void theProceduresOwnRefusalIsPassedThrough() {
        when(jdbc.query(startsWith("EXEC [SP_DoMaster]"), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(new SaleOrderDoConvertService.SpResult(0, "Sequence missing", 0, "")));

        DoConvertResult result = service.convert(12, 1);

        assertThat(result.ok()).isFalse();
        assertThat(result.message()).isEqualTo("Sequence missing");
    }

    @Test
    void theProceduresBrokenNotFoundBranchBecomesAReadableMessage() {
        DataAccessException conversion = new UncategorizedDataAccessException("bad",
                new SQLException("Conversion failed when converting the varchar value 'SaleOrder Id Not Found Issue id' to data type int.")) { };
        when(jdbc.query(startsWith("EXEC [SP_DoMaster]"), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenThrow(conversion);

        DoConvertResult result = service.convert(999, 1);

        assertThat(result.ok()).isFalse();
        assertThat(result.message()).isEqualTo("Sale order 999 was not found for this company");
    }

    @Test
    void successReturnsTheDoNumberAndPrintRows() {
        when(jdbc.query(startsWith("EXEC [SP_DoMaster]"), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(new SaleOrderDoConvertService.SpResult(1, "", 501, "DO000000501")));
        when(jdbc.query(contains("FROM DoMaster Do"), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(DoConvertResult.DoView.builder().doNo("DO000000501").jobNo("TR002601394").build()));

        DoConvertResult result = service.convert(12, 1);

        assertThat(result.ok()).isTrue();
        assertThat(result.doId()).isEqualTo(501);
        assertThat(result.doNo()).isEqualTo("DO000000501");
        assertThat(result.rows()).hasSize(1);
        assertThat(result.message()).contains("DO000000501");
    }

    @Test
    void anExistingDoIsReadBackWhenTheProcedureReportsNoId() {
        when(jdbc.query(startsWith("EXEC [SP_DoMaster]"), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(new SaleOrderDoConvertService.SpResult(1, "", 0, "")));
        when(jdbc.query(startsWith("SELECT ISNULL(DocNo"), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(77));
        when(jdbc.query(contains("FROM DoMaster Do"), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(DoConvertResult.DoView.builder().doNo("DO000000077").build()));

        DoConvertResult result = service.convert(12, 1);

        assertThat(result.ok()).isTrue();
        assertThat(result.doId()).isEqualTo(77);
    }
}
