package my.maleva.api.module.customer.service;

import my.maleva.api.integration.qne.dto.QneCustomerRequest;
import my.maleva.api.module.customer.entity.Customer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy CustomerServices.InsertCustomer field mapping — notably the
 * City column travelling as QNE's ContactPerson and OEmail/OPhone as the
 * contact points.
 */
class CustomerQnePayloadTest {

    private Customer customer() {
        Customer customer = new Customer();
        customer.setId(7);
        customer.setCustomerName("MALEVA SHIPPING SDN BHD");
        customer.setAddress1("Lot 12, Jalan Pelabuhan");
        customer.setCity("Ali bin Abu");
        customer.setOEmail("ops@maleva.example");
        customer.setOPhone("012-3456789");
        return customer;
    }

    @Test
    void mapsLegacyInsertCustomerFields() {
        QneCustomerRequest request =
                CustomerQneService.buildRequest(customer(), "MYR", "700-0000");

        assertThat(request.getCompanyName()).isEqualTo("MALEVA SHIPPING SDN BHD");
        assertThat(request.getCompanyName2()).isEqualTo("MALEVA SHIPPING SDN BHD");
        assertThat(request.getControlAccount()).isEqualTo("700-0000");
        assertThat(request.getCurrency()).isEqualTo("MYR");
        assertThat(request.getAddress1()).isEqualTo("Lot 12, Jalan Pelabuhan");
        assertThat(request.getAddress2()).isNull();
        assertThat(request.getContactPerson()).isEqualTo("Ali bin Abu");
        assertThat(request.getEmail()).isEqualTo("ops@maleva.example");
        assertThat(request.getPhoneNo1()).isEqualTo("012-3456789");
        assertThat(request.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void longAddressSpreadsAcrossFourFields() {
        Customer customer = customer();
        customer.setAddress1("A".repeat(150));

        QneCustomerRequest request =
                CustomerQneService.buildRequest(customer, "MYR", "700-0000");

        assertThat(request.getAddress1()).isEqualTo("A".repeat(100));
        assertThat(request.getAddress2()).isEqualTo("A".repeat(50));
        assertThat(request.getAddress3()).isNull();
        assertThat(request.getAddress4()).isNull();
    }
}
