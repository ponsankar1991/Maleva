package my.maleva.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SymbolMasterDto - DTO for SymbolMaster
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymbolMasterDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotBlank(message = "Symbol Name is required")
    @Size(max = 100, message = "Symbol Name must not exceed 100 characters")
    private String sName;

    @Size(max = 100, message = "Currency Name must not exceed 100 characters")
    private String cName;

    @NotNull(message = "Display Flag is required")
    private Integer dFlag;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private String modifiedBy;

    @NotNull(message = "Active status is required")
    private Integer active;

    private Float currencyValue;

    private Integer qneId;
}

