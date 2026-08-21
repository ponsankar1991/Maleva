package my.maleva.api.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * How much of one item a single truck has consumed - answers
 * "which truck used this, how much, and when".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckUsageDto {

    private Integer truckRefId;
    private String truckName;
    private BigDecimal totalIssued;
    private Long timesIssued;
    private BigDecimal sharePercent;
    private LocalDateTime lastIssuedDate;
}
