package my.maleva.api.module.invoice.view;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pins the WHERE clause the view builds, including the two legacy overrides
 * (exact search and "unpushed only" drop every other filter) and that every
 * screen value is bound rather than glued into the SQL.
 */
class SaleInvoiceViewServiceTest {

    private static SaleInvoiceViewFilter.SaleInvoiceViewFilterBuilder base() {
        return SaleInvoiceViewFilter.builder()
                .companyId(1)
                .fromDate(LocalDate.of(2026, 9, 1))
                .toDate(LocalDate.of(2026, 9, 6));
    }

    @Test
    void defaultIsInvoiceDateRangeOnly() {
        SaleInvoiceViewService.Query q = SaleInvoiceViewService.where(base().build());

        assertThat(q.sql()).isEqualTo(" AND A.SaleDate BETWEEN :fromDate AND :toDate");
        assertThat(q.params().getValue("companyId")).isEqualTo(1);
        assertThat(q.params().getValue("fromDate")).isEqualTo(LocalDate.of(2026, 9, 1));
    }

    @Test
    void everyFilterIsBoundNotConcatenated() {
        SaleInvoiceViewService.Query q = SaleInvoiceViewService.where(base()
                .customerId(7).jobTypeId(3).employeeId(9).statusId(2)
                .hideCompleted(true).remarksFilter(1)
                .offVesselName("O'BRIEN").loadingVesselName("MV STAR")
                .build());

        assertThat(q.sql())
                .contains("A.CustomerRefId = :customerId")
                .contains("A.JobMasterRefId = :jobTypeId")
                .contains("A.EmployeeRefId = :employeeId")
                .contains("A.JStatus = :statusId")
                .contains("A.JStatus <> 8")
                .contains("A.Remarks <> ''")
                .contains("A.Offvesselname LIKE :offVessel")
                .contains("A.Loadingvesselname LIKE :loadingVessel")
                .doesNotContain("O'BRIEN");
        assertThat(q.params().getValue("offVessel")).isEqualTo("%O'BRIEN%");
        assertThat(q.params().getValue("customerId")).isEqualTo(7);
    }

    @Test
    void exactSearchDropsEveryOtherFilterLikeLegacy() {
        SaleInvoiceViewService.Query invoice = SaleInvoiceViewService.where(base()
                .customerId(7).search(" INV000044235 ").build());
        assertThat(invoice.sql()).isEqualTo(" AND A.CNumberDisplay = :search");
        assertThat(invoice.params().getValue("search")).isEqualTo("INV000044235");
        assertThat(invoice.params().hasValue("customerId")).isFalse();

        SaleInvoiceViewService.Query job = SaleInvoiceViewService.where(base()
                .search("TR002601394").searchByJobNo(true).build());
        assertThat(job.sql()).isEqualTo(" AND SO.CNumberDisplay = :search");
    }

    @Test
    void unpushedOnlyDropsEveryOtherFilterLikeLegacy() {
        SaleInvoiceViewService.Query q = SaleInvoiceViewService.where(base()
                .customerId(7).search("INV1").unpushedOnly(true).build());

        assertThat(q.sql()).isEqualTo(" AND ISNULL(A.QNECode, '') = ''");
        assertThat(q.params().hasValue("search")).isFalse();
    }

    @Test
    void etaAndPickupModesSwapTheDateColumn() {
        assertThat(SaleInvoiceViewService.where(base().eta(true).etaType(1).build()).sql())
                .isEqualTo(" AND CAST(A.OETA AS date) BETWEEN :fromDate AND :toDate");
        assertThat(SaleInvoiceViewService.where(base().eta(true).etaType(2).build()).sql())
                .isEqualTo(" AND CAST(A.ETA AS date) BETWEEN :fromDate AND :toDate");
        assertThat(SaleInvoiceViewService.where(base().eta(true).etaType(0).build()).sql())
                .contains("CAST(A.ETA AS date)").contains("OR CAST(A.OETA AS date)");
        assertThat(SaleInvoiceViewService.where(base().pickup(true).build()).sql())
                .isEqualTo(" AND CAST(A.PickupDate AS date) BETWEEN :fromDate AND :toDate");
    }

    @Test
    void missingOrInvertedDatesAreRefusedWithAClearMessage() {
        assertThatThrownBy(() -> SaleInvoiceViewService.where(base().toDate(null).build()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("From date and To date are required");
        assertThatThrownBy(() -> SaleInvoiceViewService.where(base()
                .fromDate(LocalDate.of(2026, 9, 6)).toDate(LocalDate.of(2026, 9, 1)).build()))
                .hasMessageContaining("To date must not be before From date");
    }

    @Test
    void zeroIdsMeanNoFilter() {
        SaleInvoiceViewService.Query q = SaleInvoiceViewService.where(base()
                .customerId(0).jobTypeId(0).employeeId(0).statusId(0).build());
        assertThat(q.sql()).isEqualTo(" AND A.SaleDate BETWEEN :fromDate AND :toDate");
    }
}
