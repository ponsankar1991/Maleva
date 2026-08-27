package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * QNE knockoff line item (request). Legacy: KnockoffItem (Models/Transcation/ReceiptModel.cs).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneKnockoffItem {
    private String docType;
    private String docCode;
    private double payment;
    private String forexPostingDate;
    private String knockoffRefId;
}
