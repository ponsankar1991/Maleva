package my.maleva.api.module.inventory.recon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** One repair cost line as returned to the client. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconCostDto {

    private Integer id;
    private Integer reconRefId;
    private String costType;
    private String description;
    private Integer productRefId;
    private String productCode;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal rate;
    private BigDecimal amount;
    private Integer supplierRefId;
    private String supplierName;
    private String docNo;
    private LocalDateTime docDate;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate;
}
