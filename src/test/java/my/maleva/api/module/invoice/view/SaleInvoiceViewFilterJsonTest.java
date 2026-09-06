package my.maleva.api.module.invoice.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The screen may send a flag as {@code null} or leave it out; both must read
 * as the legacy default rather than a 400 ("Cannot map null into boolean").
 */
class SaleInvoiceViewFilterJsonTest {

    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();

    @Test
    void nullFlagsAndCountsFallBackToLegacyDefaults() throws Exception {
        SaleInvoiceViewFilter f = json.readValue("""
                {"companyId":6,"fromDate":"2026-08-20","toDate":"2026-08-27",
                 "customerId":null,"hideCompleted":null,"remarksFilter":null,
                 "searchByJobNo":null,"unpushedOnly":null,"eta":null,"etaType":null,"pickup":null}
                """, SaleInvoiceViewFilter.class);

        assertThat(f.getCompanyId()).isEqualTo(6);
        assertThat(f.getFromDate()).isEqualTo(LocalDate.of(2026, 8, 20));
        assertThat(f.isHideCompleted()).isFalse();
        assertThat(f.isSearchByJobNo()).isFalse();
        assertThat(f.isUnpushedOnly()).isFalse();
        assertThat(f.isEta()).isFalse();
        assertThat(f.isPickup()).isFalse();
        assertThat(f.remarksFilterOrDefault()).isZero();
        assertThat(f.etaTypeOrDefault()).isZero();

        SaleInvoiceViewService.Query q = SaleInvoiceViewService.where(f);
        assertThat(q.sql()).isEqualTo(" AND A.SaleDate BETWEEN :fromDate AND :toDate");
    }

    @Test
    void omittedKeysBehaveTheSame() throws Exception {
        SaleInvoiceViewFilter f = json.readValue(
                "{\"companyId\":6,\"fromDate\":\"2026-08-20\",\"toDate\":\"2026-08-27\"}", SaleInvoiceViewFilter.class);
        assertThat(f.isUnpushedOnly()).isFalse();
        assertThat(SaleInvoiceViewService.where(f).sql()).contains("A.SaleDate BETWEEN");
    }

    @Test
    void theFullPayloadTheScreenSendsBinds() throws Exception {
        SaleInvoiceViewFilter f = json.readValue("""
                {"companyId":6,"fromDate":"2026-08-20","toDate":"2026-08-27","customerId":0,"jobTypeId":0,
                 "employeeId":0,"statusId":0,"hideCompleted":false,"remarksFilter":0,"offVesselName":"",
                 "loadingVesselName":"","search":"","searchByJobNo":false,"unpushedOnly":true,
                 "eta":false,"etaType":0,"pickup":false}
                """, SaleInvoiceViewFilter.class);
        assertThat(f.isUnpushedOnly()).isTrue();
        assertThat(SaleInvoiceViewService.where(f).sql()).isEqualTo(" AND ISNULL(A.QNECode, '') = ''");
    }
}
