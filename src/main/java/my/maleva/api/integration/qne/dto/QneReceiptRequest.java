package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * QNE receipt insert request. Legacy: ReceiptInsertQne.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneReceiptRequest {
    private String customerCode;
    private String docDate;
    private double amount;
    private String depositAccountCode;
    private String salesPersonCode;
    private String costCentreCode;
    private String projectCode;
    private double currencyRate;
    private String docCode;
    private String description;
    private String referenceNo;
    private String bankChargesAccountCode;
    private double bankCharges;
}
