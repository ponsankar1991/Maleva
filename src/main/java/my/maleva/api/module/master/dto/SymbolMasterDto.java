package my.maleva.api.module.master.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
 * FIXED: Field names MUST match Entity field names (SName, CName, DFlag in UPPERCASE)
 * @JsonProperty controls what the API returns to clients
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymbolMasterDto {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("CompanyRefId")
    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    // ✅ FIXED: Changed from sName to SName (UPPERCASE)
    // This MUST match Entity field name for MapStruct to work
    @JsonProperty("SName")
    @NotBlank(message = "Symbol Name is required")
    @Size(max = 100, message = "Symbol Name must not exceed 100 characters")
    private String SName;

    // ✅ FIXED: Changed from cName to CName (UPPERCASE)
    @JsonProperty("CName")
    @Size(max = 100, message = "Currency Name must not exceed 100 characters")
    private String CName;

    // ✅ FIXED: Changed from dFlag to DFlag (UPPERCASE)
    @JsonProperty("DFlag")
    @NotNull(message = "Display Flag is required")
    private Integer DFlag;

    @JsonProperty("CreatedDate")
    private LocalDateTime createdDate;

    @JsonProperty("ModifiedDate")
    private LocalDateTime modifiedDate;

    @JsonProperty("ModifiedBy")
    private String modifiedBy;

    @JsonProperty("Active")
    @NotNull(message = "Active status is required")
    private Integer active;

    @JsonProperty("CurrencyValue")
    private Float currencyValue;

    @JsonProperty("QneId")
    private Integer qneId;
}