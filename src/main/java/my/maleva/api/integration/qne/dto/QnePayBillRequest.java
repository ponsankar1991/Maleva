package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * Pay bill request posted to QNE PayBills. Legacy: PaymentQNEInsertModel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QnePayBillRequest {
    private String paymentCode;
    private String paymentDate;
    private String payByAccount;
    private String referenceNo;
    private String project;
    private double currencyRate;
    private String supplier;
    private String payTo;
    private String description;
    private double totalAmount;
    @JsonProperty("IsCancelled")
    private boolean isCancelled;
    @JsonProperty("IsPostDatedCheque")
    private boolean isPostDatedCheque;
    private String chequePreparedDate;
    private String chequeDate;
    @JsonProperty("IsBouncedCheque")
    private boolean isBouncedCheque;
    private String bouncedChequeDate;
    @JsonProperty("IsTaxInclusive")
    private boolean isTaxInclusive;
    private String purchaser;
    private String costCentre;
    private String taxDate;
    private double bankChargesAmount;
    @JsonProperty("IsTaxInclusiveOnly")
    private boolean isTaxInclusiveOnly;
    private String roundingAdjustmentAccount;
    @JsonProperty("IsRounding")
    private boolean isRounding;
    private boolean postGlDescription;
}
