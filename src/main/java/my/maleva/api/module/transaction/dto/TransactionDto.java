package my.maleva.api.module.transaction.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    // Add fields as per your C# TranscationViewModel
    private Integer transactionId;
    private String transactionType;
}
