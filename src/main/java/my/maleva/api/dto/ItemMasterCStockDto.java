package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemMasterCStockDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer productRefId;

    @NotNull
    private Float cStock;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @Size(max = 20)
    private String modifiedBy;
}
