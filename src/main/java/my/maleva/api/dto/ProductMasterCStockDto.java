package my.maleva.api.dto;

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
public class ProductMasterCStockDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotNull(message = "Product Reference ID is required")
    private Integer productRefId;

    @Min(value = 0, message = "CStock must be 0 or greater")
    private Double cstock;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private String modifiedBy;
}

