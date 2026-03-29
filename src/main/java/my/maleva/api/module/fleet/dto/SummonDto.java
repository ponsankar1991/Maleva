package my.maleva.api.module.fleet.dto;

import jakarta.validation.constraints.NotBlank;
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
 * SummonDto - DTO for Summon
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummonDto {

    private Integer id;

    @NotBlank(message = "Truck Name is required")
    @Size(max = 100, message = "Truck Name must not exceed 100 characters")
    private String truckName;

    @NotBlank(message = "Driver Name is required")
    @Size(max = 100, message = "Driver Name must not exceed 100 characters")
    private String driverName;

    @NotBlank(message = "Summon is required")
    @Size(max = 200, message = "Summon must not exceed 200 characters")
    private String summon;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @Size(max = 200, message = "Document Path must not exceed 200 characters")
    private String documentPath;

    private LocalDateTime modifiedDate;

    @Size(max = 100, message = "Modified By must not exceed 100 characters")
    private String modifiedBy;

    private Integer comid;

    private LocalDate entryDate;

    @Size(max = 200, message = "Country must not exceed 200 characters")
    private String country;

    @Size(max = 200, message = "Port Pass must not exceed 200 characters")
    private String portPass;

    @Size(max = 200, message = "Truck License Mount must not exceed 200 characters")
    private String truckLcnMnt;

    @Size(max = 200, message = "Levy must not exceed 200 characters")
    private String levy;

    @Size(max = 200, message = "Fuel must not exceed 200 characters")
    private String fuel;
}

