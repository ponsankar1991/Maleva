package my.maleva.api.module.billing.bill.service;

import my.maleva.api.integration.qne.dto.QneBillLine;
import my.maleva.api.integration.qne.dto.QneBillRequest;
import my.maleva.api.module.billing.bill.entity.BillMaster;
import my.maleva.api.module.supplier.entity.Supplier;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy BillMasterConvert mapping — the local document number
 * becomes QNE's BillCode, BillFrom concatenates supplier name and address,
 * and DueDate/PostDate both carry the bill date.
 */
class BillQnePayloadTest {

    private BillMaster bill() {
        BillMaster bill = new BillMaster();
        bill.setCNumberDisplay("BL000000123");
        bill.setSaleDate(LocalDateTime.of(2026, 8, 26, 0, 0));
        bill.setRemarks("august charges");
        bill.setDescription("Port handling");
        bill.setInvoiceNo("SUP-INV-88");
        bill.setCurrencyValue(1.0f);
        return bill;
    }

    private Supplier supplier() {
        Supplier supplier = new Supplier();
        supplier.setSupplierName("PORT SERVICES SDN BHD");
        supplier.setAddress1("Wisma Port");
        supplier.setQneCode("S-0003");
        return supplier;
    }

    @Test
    void mapsLegacyBillMasterConvertFields() {
        QneBillRequest request = BillQneService.buildRequest(
                bill(), supplier(), "30 DAYS", "MYR",
                List.of(QneBillLine.builder().account("500-1000").description("Handling").amount(100.0).build()));

        assertThat(request.getBillCode()).isEqualTo("BL000000123");
        assertThat(request.getBillDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getBillFrom()).isEqualTo("PORT SERVICES SDN BHD,Wisma Port");
        assertThat(request.getSupplier()).isEqualTo("S-0003");
        assertThat(request.getReferenceNo()).isEqualTo("august charges");
        assertThat(request.getTerm()).isEqualTo("30 DAYS");
        assertThat(request.getCurrency()).isEqualTo("MYR");
        assertThat(request.getCurrencyRate()).isEqualTo(1.0);
        assertThat(request.getDescription()).isEqualTo("Port handling");
        assertThat(request.getSupplierInvNo()).isEqualTo("SUP-INV-88");
        assertThat(request.isTaxInclusive()).isFalse();
        assertThat(request.isRounding()).isFalse();
        // Payment terms are never applied — both dates are the bill date.
        assertThat(request.getDueDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getPostDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getDetails()).hasSize(1);
    }

    @Test
    void billFromToleratesMissingAddress() {
        Supplier supplier = supplier();
        supplier.setAddress1(null);
        assertThat(BillQneService.buildRequest(bill(), supplier, "", "", List.of()).getBillFrom())
                .isEqualTo("PORT SERVICES SDN BHD,");
    }
}
