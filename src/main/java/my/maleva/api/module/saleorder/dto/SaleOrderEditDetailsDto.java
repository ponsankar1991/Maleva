package my.maleva.api.module.saleorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Enriched sale-order detail row for edit screens. It keeps the persisted
 * sale-order detail columns and adds lookup data from ItemMaster, TaxMaster,
 * and UOM.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SaleOrderEditDetailsDto extends SaleOrderDetailsDto {

    private String productCode;

    private String productName;

    private String taxCode;

    private String uom;

    private String saleJobNo;
}
