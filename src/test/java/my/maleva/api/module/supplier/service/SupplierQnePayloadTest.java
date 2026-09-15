package my.maleva.api.module.supplier.service;

import my.maleva.api.integration.qne.dto.QneSupplierRequest;
import my.maleva.api.module.supplier.dto.SupplierDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy SupplierServices.InsertSupplier field mapping — the mirror
 * of the customer push, with the four Is* flags hardcoded false.
 */
class SupplierQnePayloadTest {

    @Test
    void mapsLegacyInsertSupplierFields() {
        SupplierDto typed = new SupplierDto();
        typed.setSupplierName("Port Services Sdn Bhd");
        typed.setAddress1("Wisma Port, Jalan Dua");
        typed.setCity("Siti");
        typed.setOEmail("billing@port.example");
        typed.setOPhone("019-8765432");

        QneSupplierRequest request = SupplierQneService.buildRequest(typed, "USD", "800-2000");

        // As typed — not the upper-cased copy SP_Supplier stores.
        assertThat(request.getCompanyName()).isEqualTo("Port Services Sdn Bhd");
        assertThat(request.getCompanyName2()).isEqualTo("Port Services Sdn Bhd");
        assertThat(request.getControlAccount()).isEqualTo("800-2000");
        assertThat(request.getCurrency()).isEqualTo("USD");
        assertThat(request.getAddress1()).isEqualTo("Wisma Port, Jalan Dua");
        assertThat(request.getAddress2()).isNull();
        assertThat(request.getContactPerson()).isEqualTo("Siti");
        assertThat(request.getEmail()).isEqualTo("billing@port.example");
        assertThat(request.getPhoneNo1()).isEqualTo("019-8765432");
        assertThat(request.isProspect()).isFalse();
        assertThat(request.isSuspended()).isFalse();
        assertThat(request.isExceedCreditAllowed()).isFalse();
        assertThat(request.isTaxExempted()).isFalse();
    }

    @Test
    void splitsLongAddressIntoHundredCharChunks() {
        SupplierDto typed = new SupplierDto();
        typed.setAddress1("A".repeat(100) + "B".repeat(100) + "C".repeat(20));

        QneSupplierRequest request = SupplierQneService.buildRequest(typed, "", "800-2000");

        assertThat(request.getAddress1()).isEqualTo("A".repeat(100));
        assertThat(request.getAddress2()).isEqualTo("B".repeat(100));
        assertThat(request.getAddress3()).isEqualTo("C".repeat(20));
        assertThat(request.getAddress4()).isNull();
    }
}
