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
public class BillsOrderDetailsDto {
    private Integer id;

    @NotNull
    private Integer billsOrderMasterRefId;

    @NotNull
    private Integer accountMasterRefId;

    @NotNull
    private Float mrp;

    @NotNull
    private Float purchaseRate;

    @NotNull
    private Float itemQty;

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

    @Size(max = 300)
    private String remarksD;

    @NotNull
    private Float currencyValue;

    @NotNull
    private Float actualAmount;

    private Integer productRefId;
    private Float quoteValue;

    @Size(max = 150)
    private String serialNo;
}
