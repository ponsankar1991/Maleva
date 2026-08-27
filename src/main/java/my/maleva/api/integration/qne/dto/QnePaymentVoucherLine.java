package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * Payment voucher detail line request. Legacy: PaymentVoucherQNEInsertDetailModel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QnePaymentVoucherLine {
    private String account;
    private String description;
    private double amount;
    private String project;
    private String referenceNo;
    private String registrationTin;
    private String supplier;
    private String address;
    private String costCentre;
    private String taxCode;
    @JsonProperty("IsTaxInclusive")
    private boolean isTaxInclusive;
    @JsonProperty("WTaxCode")
    private String wTaxCode;
}
