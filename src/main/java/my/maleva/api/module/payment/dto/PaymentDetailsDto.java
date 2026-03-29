package my.maleva.api.module.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetailsDto {
    private Integer id;

    private Integer companyRefId;

    @NotNull
    private Integer paymentRefId;

    private Integer purchaseMasterRefId;

    private Integer supplieropenRefId;

    @NotNull
    private BigDecimal paymentAmount;

    @NotNull
    private LocalDateTime createdDate;

    private Integer billMasterRefId;

    @NotNull
    private Float currencyValue;

    @NotNull
    private Float actualAmount;
}
