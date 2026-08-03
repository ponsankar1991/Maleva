package my.maleva.api.module.saleorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SaleOrderRemarksUpdateDto {
    
    @NotNull(message = "Job ID is required")
    private Integer id;
    
    @NotNull(message = "Company ID is required")
    private Integer companyRefId;
    
    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;
}
