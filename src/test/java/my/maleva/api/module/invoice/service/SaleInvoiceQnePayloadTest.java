package my.maleva.api.module.invoice.service;

import my.maleva.api.integration.qne.dto.QneSalesInvoiceLine;
import my.maleva.api.integration.qne.dto.QneSalesInvoiceRequest;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.invoice.entity.SaleDetails;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy SaleInvoiceServices.InvoiceConvert header and line mapping,
 * including the multi-reference blanking rule and the degenerate Title CASE.
 */
class SaleInvoiceQnePayloadTest {

    private SaleMaster invoice() {
        SaleMaster invoice = new SaleMaster();
        invoice.setId(42);
        invoice.setSaleDate(LocalDateTime.of(2026, 8, 26, 0, 0));
        invoice.setRemarks1("REF-2026-001");
        invoice.setCurrencyValue(4.45);
        invoice.setOrigin("PORT KLANG");
        invoice.setDestination("SINGAPORE");
        invoice.setQuantity("120");
        invoice.setTotalWeight("3400");
        invoice.setOffvesselname("MV OFFSHORE");
        invoice.setLoadingvesselname("MV LOADER");
        invoice.setCommodity("PALM OIL");
        return invoice;
    }

    private Customer customer() {
        Customer customer = new Customer();
        customer.setCompanyCode("C-0007");
        customer.setCity("Ali bin Abu");
        customer.setCustomerName("MALEVA SHIPPING");
        return customer;
    }

    @Test
    void singleReferenceMapsVesselFieldsIntoRefsAndRemarks() {
        QneSalesInvoiceRequest request = SaleInvoiceQneService.buildRequest(
                invoice(), customer(), "30 DAYS", false, "DO-000123", List.of());

        assertThat(request.getCustomer()).isEqualTo("C-0007");
        assertThat(request.getInvoiceDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getTerm()).isEqualTo("30 DAYS");
        assertThat(request.getAttention()).isEqualTo("Ali bin Abu");
        assertThat(request.getReferenceNo()).isEqualTo("REF-2026-001");
        assertThat(request.getCurrencyRate()).isEqualTo(4.45);
        assertThat(request.getRef1()).isEqualTo("PORT KLANG");
        assertThat(request.getRef2()).isEqualTo("SINGAPORE");
        assertThat(request.getRef3()).isEqualTo("120");
        assertThat(request.getRef4()).isEqualTo("3400");
        assertThat(request.getRef5()).isEqualTo("MV OFFSHORE");
        assertThat(request.getRemark1()).isEqualTo("MV LOADER");
        assertThat(request.getRemark2()).isEqualTo("DO-000123");
        assertThat(request.getRemark3()).isEqualTo("PALM OIL");
        assertThat(request.getTitle()).isEqualTo("MV LOADER");
        // The SaleInvoice screen's values — the SaleOrder conversion sent
        // true/true to the same endpoint (catalog ambiguity #1).
        assertThat(request.isTaxInclusive()).isFalse();
        assertThat(request.isRounding()).isFalse();
    }

    @Test
    void multiReferenceBlanksRefsRemarksAndTitleButKeepsAttentionAndReference() {
        QneSalesInvoiceRequest request = SaleInvoiceQneService.buildRequest(
                invoice(), customer(), "30 DAYS", true, "DO-000123", List.of());

        assertThat(request.getRef1()).isEmpty();
        assertThat(request.getRef2()).isEmpty();
        assertThat(request.getRef3()).isEmpty();
        assertThat(request.getRef4()).isEmpty();
        assertThat(request.getRef5()).isEmpty();
        assertThat(request.getRemark1()).isEmpty();
        assertThat(request.getRemark2()).isEmpty();
        assertThat(request.getRemark3()).isEmpty();
        assertThat(request.getTitle()).isEmpty();
        assertThat(request.getAttention()).isEqualTo("Ali bin Abu");
        assertThat(request.getReferenceNo()).isEqualTo("REF-2026-001");
    }

    @Test
    void titleIsSalesOnlyWhenBothVesselsAreBlank() {
        SaleMaster noVessels = invoice();
        noVessels.setLoadingvesselname("");
        noVessels.setOffvesselname("");
        assertThat(SaleInvoiceQneService.buildRequest(
                noVessels, customer(), "", false, null, List.of()).getTitle())
                .isEqualTo("SALES");

        // Legacy's degenerate CASE: an empty loading vessel yields an empty
        // title even when the off vessel is named.
        SaleMaster offOnly = invoice();
        offOnly.setLoadingvesselname("");
        assertThat(SaleInvoiceQneService.buildRequest(
                offOnly, customer(), "", false, null, List.of()).getTitle())
                .isEmpty();
    }

    @Test
    void lineEncodesStockCodeAndFallsBackToProductName() {
        SaleDetails detail = new SaleDetails();
        detail.setItemQty(2.0);
        detail.setSalesRate(500.0);
        detail.setSdRemarks(null);

        ItemMaster item = new ItemMaster();
        item.setProdCode("OIL&GAS");
        item.setPName("OIL AND GAS HANDLING");

        QneSalesInvoiceLine line = SaleInvoiceQneService.buildLine(
                detail, item, "TRIP", "2026-08-26T00:00:00");

        assertThat(line.getStock()).isEqualTo("OIL&amp;GAS");
        assertThat(line.getDescription()).isEqualTo("OIL AND GAS HANDLING");
        assertThat(line.getQty()).isEqualTo(2.0);
        assertThat(line.getUom()).isEqualTo("TRIP");
        assertThat(line.getUnitPrice()).isEqualTo(500.0);
        assertThat(line.getDateRef1()).isEqualTo("2026-08-26T00:00:00");
        assertThat(line.getDateRef2()).isEqualTo("2026-08-26T00:00:00");
        assertThat(line.getTransferFrom()).isNotNull();

        detail.setSdRemarks("SPECIAL HANDLING");
        assertThat(SaleInvoiceQneService.buildLine(detail, item, "TRIP", "2026-08-26T00:00:00")
                .getDescription()).isEqualTo("SPECIAL HANDLING");
    }
}
