package my.maleva.api.module.ceodashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Response containing the minimum and maximum available dates for filtering")
public class DateRangeResponseDto {
    @Schema(description = "The earliest available sale date in the system", example = "2020-01-01")
    private LocalDate minDate;
    
    @Schema(description = "The latest available sale date in the system", example = "2026-12-31")
    private LocalDate maxDate;
}
