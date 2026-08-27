package my.maleva.api.module.paymentrecept.service;

import my.maleva.api.integration.qne.dto.QneReceiptRequest;
import my.maleva.api.module.customer.entity.Customer;
import my.maleva.api.module.paymentrecept.entity.Receipt;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pins the legacy ReceiptServices.ReceiptVIEW header mapping — the deposit
 * account is the bank's QNE code and the rate is the receipt's CurrencyValue.
 */
class ReceiptQnePayloadTest {

    @Test
    void mapsLegacyReceiptViewFields() {
        Receipt receipt = new Receipt();
        receipt.setReceiptDate(LocalDateTime.of(2026, 8, 26, 0, 0));
        receipt.setAmount(new BigDecimal("1234.56"));
        receipt.setBankCharges(2.5);
        receipt.setCurrencyValue(1.0);

        Customer customer = new Customer();
        customer.setCompanyCode("C-0007");

        QneReceiptRequest request =
                ReceiptQneService.buildRequest(receipt, customer, "310-0001");

        assertThat(request.getCustomerCode()).isEqualTo("C-0007");
        assertThat(request.getDocDate()).isEqualTo("2026-08-26T00:00:00");
        assertThat(request.getAmount()).isEqualTo(1234.56);
        assertThat(request.getBankCharges()).isEqualTo(2.5);
        assertThat(request.getDepositAccountCode()).isEqualTo("310-0001");
        assertThat(request.getCurrencyRate()).isEqualTo(1.0);
    }
}
