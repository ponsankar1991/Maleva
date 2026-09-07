package my.maleva.api.module.paymentrecept.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.module.paymentrecept.dto.ReceiptSaveRequest;
import my.maleva.api.module.paymentrecept.dto.ReceiptSaveResponseDto;
import my.maleva.api.module.paymentrecept.service.ReceiptService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * The insert endpoint used to declare its body as a Jackson 2 {@code JsonNode},
 * which the Jackson 3 HTTP layer of Spring Boot 4 cannot construct — every
 * save died with "no Creators, like default constructor, exist" before any
 * receipt code ran. The body is now bound as a plain Map/List; this pins the
 * conversion of both shapes (the React object and the legacy one-element
 * array) into ReceiptSaveRequest, aliases included.
 */
class ReceiptControllerInsertBindingTest {

    private final ReceiptService service = Mockito.mock(ReceiptService.class);
    private final ReceiptController controller = controller();

    @Test
    void bindsTheReactObjectBody() {
        Map<String, Object> body = Map.of(
                "id", 0, "companyRefId", 6, "customerRefId", 17, "bankRefId", 3,
                "receiptDate", "2026-09-07", "amount", 742.39, "currencyValue", 3.08,
                "receiptDetails", List.of(Map.of("saleMasterRefId", 14804, "amount", 742.39, "currencyValue", 3.08)));

        controller.insertReceipt(body, null, null, 6);

        ReceiptSaveRequest sent = captured();
        assertThat(sent.getCustomerRefId()).isEqualTo(17);
        assertThat(sent.getAmount()).isEqualByComparingTo(new BigDecimal("742.39"));
        assertThat(sent.getReceiptDetails()).hasSize(1);
        assertThat(sent.getReceiptDetails().get(0).getSaleMasterRefId()).isEqualTo(14804);
    }

    @Test
    void bindsTheLegacyArrayBodyWithPascalCaseNames() {
        List<Map<String, Object>> body = List.of(Map.of(
                "Id", 0, "CompanyRefId", 6, "CustomerRefId", 17, "BankRefId", 3,
                "ReceiptDate", "2026-09-07", "Amount", "742.39",
                "ReceiptDetails", List.of(Map.of("SaleMasterRefId", 14804, "Amount", "742.39"))));

        controller.insertReceipt(body, 6, null, null);

        ReceiptSaveRequest sent = captured();
        assertThat(sent.getBankRefId()).isEqualTo(3);
        assertThat(sent.getReceiptDetails().get(0).getAmount()).isEqualByComparingTo(new BigDecimal("742.39"));
    }

    private ReceiptSaveRequest captured() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReceiptSaveRequest>> captor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(service).insertReceipt(captor.capture(), anyInt());
        assertThat(captor.getValue()).hasSize(1);
        return captor.getValue().get(0);
    }

    private ReceiptController controller() {
        when(service.insertReceipt(anyList(), anyInt()))
                .thenReturn(ReceiptSaveResponseDto.builder().ok(true).isSuccess(true).id(1).name("RC000000001").build());
        ReceiptController c = new ReceiptController();
        ReflectionTestUtils.setField(c, "receiptService", service);
        ReflectionTestUtils.setField(c, "objectMapper", new ObjectMapper());
        return c;
    }
}
