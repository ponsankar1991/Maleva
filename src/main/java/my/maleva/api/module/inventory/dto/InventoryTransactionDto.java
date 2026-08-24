package my.maleva.api.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionDto {

    private Integer id;
    private Integer companyRefId;
    private Integer productRefId;
    private String productCode;
    private String productName;
    private String transactionType;
    private BigDecimal quantity;
    private BigDecimal balanceAfter;
    private BigDecimal unitCost;
    private BigDecimal totalValue;
    private String referenceType;
    private Integer referenceId;
    private Integer truckRefId;
    private String truckName;
    private String assetSerialNo;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate;
}
