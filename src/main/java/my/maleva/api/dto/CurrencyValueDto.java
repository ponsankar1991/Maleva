package my.maleva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CurrencyValueDto - DTO for Currency Value response
 * Returned from GetCurrencyValue API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyValueDto {
    private Double currencyValue;
    private Integer symbolRefId;
}

