package my.maleva.api.module.productmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for GetProductList API response
 * Fields: Id, ProductName, SaleRate, PurRate, MRP, ProductCode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductListDto {
    private Integer id;
    private String productName;
    private Float saleRate;
    private Float purRate;
    private Float mrp;
    private String productCode;
}

