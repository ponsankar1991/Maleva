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
public class CustomerQuotationDetailsDto {
    private Integer id;

    @NotNull
    private Integer customerQuotationRefId;

    private Integer itemMasterRefId;
    private Integer mrp;
    private Integer purchaseRate;
    private Integer itemQty;

    @NotNull
    private Float discPer;

    @NotNull
    private Float discAmount;

    @NotNull
    private Float landingCost;

    @NotNull
    private Float taxPercent;

    @NotNull
    private Float taxAmount;

    @NotNull
    private Float salesRate;

    @NotNull
    private Float netSalesRate;

    @NotNull
    private Float amount;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    private Float currencyValue;
    private Float actualAmount;

    @Size(max = 300)
    private String sdRemarks;

    private Integer taxRefId;

    @Size(max = 300)
    private String productName;

    @Size(max = 300)
    private String rowType;

    @Size(max = 300)
    private String groupKey;

    private Integer orderNo;
}
