package my.maleva.api.module.salecreditmaster.service;

import my.maleva.api.integration.qne.dto.QneSalesCnLine;
import my.maleva.api.integration.qne.dto.QneSalesCnRequest;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.itemmaster.entity.ItemMaster;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditDetails;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy SaleCreditServices.SaleCreditVIEW mapping — hardcoded
 * true/true tax flags, ReferenceNo carrying the original invoice's QNE code,
 * and CN lines described by product name only.
 */
class SaleCreditQnePayloadTest {

    @Test
    void mapsLegacySaleCreditViewFields() {
        SaleCreditMaster creditNote = new SaleCreditMaster();
        creditNote.setSaleDate(LocalDateTime.of(2026, 8, 26, 0, 0));
        creditNote.setCurrencyValue(1.0);

        Customer customer = new Customer();
        customer.setCompanyCode("C-0007");
        customer.setCity("Ali bin Abu");

        QneSalesCnRequest request = SaleCreditQneService.buildRequest(
                creditNote, customer, "30 DAYS", "IV-000456", List.of());

        assertThat(request.getCustomer()).isEqualTo("C-0007");
        assertThat(request.getCnDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getTerm()).isEqualTo("30 DAYS");
        assertThat(request.getReferenceNo()).isEqualTo("IV-000456");
        assertThat(request.getAttention()).isEqualTo("Ali bin Abu");
        assertThat(request.getCurrencyRate()).isEqualTo(1.0);
        assertThat(request.isTaxInclusive()).isTrue();
        assertThat(request.isRounding()).isTrue();
    }

    @Test
    void lineUsesProductNameWithoutRemarksOverride() {
        SaleCreditDetails detail = new SaleCreditDetails();
        detail.setItemQty(1.0);
        detail.setSalesRate(250.0);

        ItemMaster item = new ItemMaster();
        item.setProdCode("FRT&SEA");
        item.setPName("SEA FREIGHT");

        QneSalesCnLine line = SaleCreditQneService.buildLine(
                detail, item, "TRIP", "2026-08-26T00:00:00");

        assertThat(line.getStock()).isEqualTo("FRT&amp;SEA");
        assertThat(line.getDescription()).isEqualTo("SEA FREIGHT");
        assertThat(line.getQty()).isEqualTo(1.0);
        assertThat(line.getUom()).isEqualTo("TRIP");
        assertThat(line.getUnitPrice()).isEqualTo(250.0);
        assertThat(line.getDateRef1()).isEqualTo("2026-08-26T00:00:00");
        assertThat(line.getDateRef2()).isEqualTo("2026-08-26T00:00:00");
    }
}
