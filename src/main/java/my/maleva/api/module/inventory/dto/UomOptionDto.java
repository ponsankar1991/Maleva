package my.maleva.api.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One choice for the unit picker on the new-product form. Carries the UOM
 * master id that ProductMaster.UOM_Code must point at, plus the label the
 * store sees.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UomOptionDto {

    private Integer uomCode;
    private String code;
    private String description;
}
