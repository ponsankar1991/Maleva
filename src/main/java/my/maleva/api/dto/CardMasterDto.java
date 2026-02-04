package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardMasterDto {
    private Integer id;

    @NotBlank
    @Size(max = 50)
    private String cardType;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal scharge;

    private LocalDateTime createdDate;

    @NotBlank
    @Size(max = 200)
    private String createdBy;

    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 200)
    private String modifiedBy;

    @NotNull
    private Integer companyRefId;
}
