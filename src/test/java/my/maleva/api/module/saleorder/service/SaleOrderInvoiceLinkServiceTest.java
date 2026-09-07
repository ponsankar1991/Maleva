package my.maleva.api.module.saleorder.service;

import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceLink;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class SaleOrderInvoiceLinkServiceTest {

    private final NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
    private final SaleOrderInvoiceLinkService service = new SaleOrderInvoiceLinkService(jdbc);

    /** Only the job lookup joins Customer on the sale order's own alias. */
    private static final String JOB_SQL = "ON C.Id = S.CustomerRefId";

    private void saleOrderExists() {
        // The type witness matters: List.of(new String[]{...}) is varargs and
        // would build a list of two Strings, not one String[].
        when(jdbc.query(contains(JOB_SQL), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.<String[]>of(new String[]{"TR002601394", "NX TRANSPORT SERVICE (M) SDN BHD"}));
    }

    @Test
    void anUnknownSaleOrderIsEmptySoTheScreenCanSay404() {
        when(jdbc.query(contains(JOB_SQL), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        assertThat(service.find(999, 6)).isEmpty();
    }

    @Test
    void badArgumentsNeverReachTheDatabase() {
        assertThat(service.find(0, 6)).isEmpty();
        assertThat(service.find(12, null)).isEmpty();
        verify(jdbc, never()).query(anyString(), any(SqlParameterSource.class), any(RowMapper.class));
    }

    @Test
    void anUninvoicedJobStillNamesItselfForTheConfirmation() {
        saleOrderExists();
        when(jdbc.query(contains("FROM SaleMaster SM"), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        SaleOrderInvoiceLink link = service.find(12, 6).orElseThrow();

        assertThat(link.invoiced()).isFalse();
        assertThat(link.jobNo()).isEqualTo("TR002601394");
        assertThat(link.customerName()).isEqualTo("NX TRANSPORT SERVICE (M) SDN BHD");
        assertThat(link.invoiceId()).isNull();
    }

    @Test
    void anInvoicedJobNamesTheInvoice() {
        saleOrderExists();
        when(jdbc.query(contains("FROM SaleMaster SM"), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of(new SaleOrderInvoiceLink(12, "TR002601394", "NX TRANSPORT SERVICE (M) SDN BHD",
                        true, 15897, "INV000044006", "27/08/2026", "INV000044006", "8TCBM57R389YDSAVW6YEH01M10")));

        SaleOrderInvoiceLink link = service.find(12, 6).orElseThrow();

        assertThat(link.invoiced()).isTrue();
        assertThat(link.invoiceId()).isEqualTo(15897);
        assertThat(link.invoiceNo()).isEqualTo("INV000044006");
        assertThat(link.invoiceDate()).isEqualTo("27/08/2026");
    }

    @Test
    void theQueryIsScopedToTheCompanyAndCountsAllThreeLinks() {
        saleOrderExists();
        when(jdbc.query(contains("FROM SaleMaster SM"), any(SqlParameterSource.class), any(RowMapper.class)))
                .thenReturn(List.of());

        service.find(12, 6);

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<SqlParameterSource> params = ArgumentCaptor.forClass(SqlParameterSource.class);
        verify(jdbc).query(startsWith("SELECT TOP 1 SM.Id"), params.capture(), any(RowMapper.class));
        verify(jdbc, org.mockito.Mockito.atLeastOnce()).query(sql.capture(), any(SqlParameterSource.class), any(RowMapper.class));

        String invoiceSql = sql.getAllValues().stream().filter(s -> s.contains("FROM SaleMaster SM")).findFirst().orElseThrow();
        assertThat(invoiceSql)
                .contains("SM.CompanyRefId = :companyId")
                .contains("SM.Active = 1")
                .contains("SaleDetails SD")
                .contains("SaleMasterReference R")
                .contains("SO.InvoiceNo = SM.Id");
        assertThat(params.getValue().getValue("companyId")).isEqualTo(6);
        assertThat(params.getValue().getValue("saleOrderId")).isEqualTo(12);
    }
}
