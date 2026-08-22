package my.maleva.api.module.inventory.recon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Recon spend for one truck, for the cost-per-truck report. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckReconSummaryDto {

    private Integer truckRefId;
    private String truckName;
    private Long jobCount;
    private BigDecimal totalCost;
}
