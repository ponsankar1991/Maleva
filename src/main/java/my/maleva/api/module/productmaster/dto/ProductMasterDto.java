package my.maleva.api.module.productmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMasterDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotBlank(message = "Product Code is required")
    @Size(max = 50, message = "Product Code cannot exceed 50 characters")
    private String prodCode;

    private Integer pcodeDigits;

    @NotBlank(message = "Product Name is required")
    @Size(max = 100, message = "Product Name cannot exceed 100 characters")
    private String pname;

    @Size(max = 100, message = "Print Name cannot exceed 100 characters")
    private String printName;

    @Size(max = 100, message = "Second Product Code cannot exceed 100 characters")
    private String secondPCode;

    @Size(max = 100, message = "HSN Code cannot exceed 100 characters")
    private String hsnCode;

    @NotNull(message = "Tax Code is required")
    private Integer taxCode;

    @NotNull(message = "UOM Code is required")
    private Integer uomCode;

    @Min(value = 0, message = "MRP must be 0 or greater")
    private Double mrp;

    @Min(value = 0, message = "Purchase Rate must be 0 or greater")
    private Double purchaseRate;

    @Min(value = 0, message = "Landing Cost must be 0 or greater")
    private Double landingCost;

    @Min(value = 0, message = "Sales Rate must be 0 or greater")
    private Double salesRate;

    private Boolean saleRateType;

    @Size(max = 100, message = "Remarks cannot exceed 100 characters")
    private String remarks;

    private Integer activestatus;

    private Integer sorting;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private String modifiedBy;

    private Integer isProduct;
}

