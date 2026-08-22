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
public class InventoryAssetDto {

    private Integer id;
    private Integer companyRefId;
    private Integer productRefId;
    private String prodCode;
    private String pname;
    private String serialNo;
    private String status;
    private Integer currentTruckRefId;
    private String currentTruckName;

    /** NEW until the first completed recon, RECON from then on. */
    private String condition;

    /** How many times this unit has been reconditioned. */
    private Integer reconCount;

    /** Where it came off. Survives removal, unlike currentTruckRefId. */
    private Integer lastTruckRefId;
    private String lastTruckName;

    /** Purchase cost while NEW; the repair spend once RECON. */
    private BigDecimal currentValue;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
}
