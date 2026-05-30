package my.maleva.api.module.patmentvouchmaster.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PaymentVoucherComboResponse - Response wrapper for SelectPaymentTo endpoint
 * Format: { "ok": true/false, "message": "...", "data": [...] }
 * Compatible with .NET migration guide
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherComboResponse {

    @JsonProperty("ok")
    private boolean ok;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private List<PaymentVoucherComboDto> data;

    /**
     * Create a successful response with data
     */
    public static PaymentVoucherComboResponse success(List<PaymentVoucherComboDto> data) {
        return PaymentVoucherComboResponse.builder()
                .ok(true)
                .message("Success")
                .data(data)
                .build();
    }

    /**
     * Create an error response
     */
    public static PaymentVoucherComboResponse error(String message) {
        return PaymentVoucherComboResponse.builder()
                .ok(false)
                .message(message)
                .data(null)
                .build();
    }
}

