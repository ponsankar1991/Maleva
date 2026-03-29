package my.maleva.api.module.pettycash.dto;

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
public class PettyCashDetailDto {
    private Integer id;

    @Size(max = 100)
    private String items;

    private BigDecimal amount;

    @Size(max = 255)
    private String notes;

    @NotNull
    private Integer pettyCashMasterRefId;

    private Integer active;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
