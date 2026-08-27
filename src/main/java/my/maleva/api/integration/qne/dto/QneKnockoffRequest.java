package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

/**
 * QNE receipt knockoff insert request. Legacy: ReceiptKnockOffInsertQne.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneKnockoffRequest {
    private String docId;
    private List<QneKnockoffItem> knockoffItems;
}
