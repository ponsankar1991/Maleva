package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherDetailsDto {
    private Integer id;

    @NotNull
    private Integer paymentVoucherMasterRefId;

    @NotNull
    private Integer accountGroupRefId;

    @Size(max = 300)
    private String description;

    @NotNull
    private Float amount;

    @NotNull
    private LocalDateTime createdDate;

    @NotNull
    private LocalDateTime modifiedDate;

    @NotNull
    private Float currencyValue;

    @NotNull
    private Float actualAmount;

    private Integer subExpenseRefid;

    private Integer pendingPaymentRefId;

    private Integer classification;
}
