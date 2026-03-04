package my.maleva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * CurrencyValueDto - DTO for Currency Value response
 * Returned from GetCurrencyValue API
 *
 * Uses BigDecimal for currencyValue to ensure precise decimal representation
 * Prevents floating-point precision issues (e.g., 3.08 becoming 3.0799999237060547)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyValueDto {
    private BigDecimal currencyValue;  // ← Changed from Double to BigDecimal
    private Integer symbolRefId;
}

