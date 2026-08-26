package my.maleva.api.module.joborder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOrderDetailResponseDto {

    private Integer id;
    private Integer jobOrderMasterRefId;
    private String problemName;
    private String productUse;
    private Integer productRefId;
    private Integer supplierMasterRefId;
    private String supplierName;
    private BigDecimal cost;
    private BigDecimal quantity;
    private Integer inventoryTransactionRefId;
    /** Purchase order this line's part was bought on; null when taken from stock. */
    private Integer billsOrderMasterRefId;

    private String remarks;
    private Integer active;
    
    private Integer createdBy;
    private LocalDateTime createdDate;
    private Integer modifiedBy;
    private LocalDateTime modifiedDate;
}
