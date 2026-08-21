package my.maleva.api.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
}
