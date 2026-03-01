package my.maleva.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TruckSparePartsDto - DTO for TruckSpareParts
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckSparePartsDto {

    private Integer id;

    @NotBlank(message = "Truck Name is required")
    @Size(max = 100, message = "Truck Name must not exceed 100 characters")
    private String truckName;

    @NotBlank(message = "Driver Name is required")
    @Size(max = 100, message = "Driver Name must not exceed 100 characters")
    private String driverName;

    @NotBlank(message = "Spare Parts is required")
    @Size(max = 200, message = "Spare Parts must not exceed 200 characters")
    private String spareParts;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 200, message = "Document Path must not exceed 200 characters")
    private String documentPath;

    private LocalDateTime modifiedDate;

    private String modifiedBy;

    private Integer comid;

    private LocalDate entryDate;
}

