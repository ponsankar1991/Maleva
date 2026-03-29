package my.maleva.api.module.saleorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SaleOrderBONotRequiredDto - DTO for SaleOrderBONotRequired
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderBONotRequiredDto {

    private Integer id;

    private Integer saleOrderMasterRefId;

    private Integer boTypeId;
}

