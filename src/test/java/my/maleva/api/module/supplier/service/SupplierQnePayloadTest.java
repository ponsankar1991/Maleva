package my.maleva.api.module.supplier.service;

import my.maleva.api.integration.qne.dto.QneSupplierRequest;
import my.maleva.api.module.supplier.entity.Supplier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy SupplierServices.InsertSupplier field mapping — the mirror
 * of the customer push, with the four Is* flags hardcoded false.
 */
class SupplierQnePayloadTest {

    @Test
    void mapsLegacyInsertSupplierFields() {
        Supplier supplier = new Supplier();
        supplier.setId(3);
        supplier.setSupplierName("PORT SERVICES SDN BHD");
        supplier.setAddress1("Wisma Port, Jalan Dua");
        supplier.setCity("Siti");
        supplier.setOEmail("billing@port.example");
        supplier.setOPhone("019-8765432");

        QneSupplierRequest request =
                SupplierQneService.buildRequest(supplier, "USD", "800-2000");

        assertThat(request.getCompanyName()).isEqualTo("PORT SERVICES SDN BHD");
        assertThat(request.getCompanyName2()).isEqualTo("PORT SERVICES SDN BHD");
        assertThat(request.getControlAccount()).isEqualTo("800-2000");
        assertThat(request.getCurrency()).isEqualTo("USD");
        assertThat(request.getAddress1()).isEqualTo("Wisma Port, Jalan Dua");
        assertThat(request.getContactPerson()).isEqualTo("Siti");
        assertThat(request.getEmail()).isEqualTo("billing@port.example");
        assertThat(request.getPhoneNo1()).isEqualTo("019-8765432");
        assertThat(request.isProspect()).isFalse();
        assertThat(request.isSuspended()).isFalse();
        assertThat(request.isExceedCreditAllowed()).isFalse();
        assertThat(request.isTaxExempted()).isFalse();
    }
}
