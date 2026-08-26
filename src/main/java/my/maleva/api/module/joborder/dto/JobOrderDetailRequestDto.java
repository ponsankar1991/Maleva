package my.maleva.api.module.joborder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOrderDetailRequestDto {

    private Integer id;

    private Integer jobOrderMasterRefId;

    @NotBlank(message = "Problem Name is required")
    @Size(max = 200, message = "Problem name cannot exceed 200 characters")
    private String problemName;

    @Size(max = 200, message = "Product use cannot exceed 200 characters")
    private String productUse;

    private Integer productRefId;

    private Integer supplierMasterRefId;

    private BigDecimal cost;

    /** Units consumed; treated as 1 when absent, matching every pre-quantity row. */
    private BigDecimal quantity;

    /** Purchase order this line's part was bought on; null when taken from stock. */
    private Integer billsOrderMasterRefId;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    private Integer active;
}
