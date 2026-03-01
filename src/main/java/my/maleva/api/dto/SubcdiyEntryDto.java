package my.maleva.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SubcdiyEntryDto - DTO for SubcdiyEntry
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcdiyEntryDto {

    private Integer id;

    @Size(max = 200, message = "Actual Amount must not exceed 200 characters")
    private String actualAmount;

    private BigDecimal amount;

    private LocalDate entryDate;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    @NotNull(message = "Active status is required")
    private Integer active;
}

