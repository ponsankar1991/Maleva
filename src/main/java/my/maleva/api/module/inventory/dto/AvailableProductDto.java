package my.maleva.api.module.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A product that exists in ProductMaster but has no workshop settings yet,
 * so it can be picked when adding an inventory item instead of being typed
 * in again as a duplicate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailableProductDto {

    private Integer productRefId;
    private String prodCode;
    private String pname;
    private Integer uomCode;
    private Double purchaseRate;
}
