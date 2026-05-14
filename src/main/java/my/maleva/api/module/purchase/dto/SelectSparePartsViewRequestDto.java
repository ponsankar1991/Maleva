package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for SelectSparePartsView operation
 * Contains all filter parameters for the spare parts report query
 * Equivalent to request parameters in .NET SelectSparePartsView method
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectSparePartsViewRequestDto {

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    @JsonProperty("Comid")
    private Integer companyId;

    @JsonProperty("Fromdate")
    private String fromDate;

    @JsonProperty("Todate")
    private String toDate;

    @JsonProperty("Id")
    private Integer supplierId;

    @JsonProperty("Employeeid")
    private Integer employeeId;

    @JsonProperty("Search")
    private String search;

    @JsonProperty("invoicecheck")
    private Integer invoiceCheck;

    @JsonProperty("DriverId")
    private Integer driverId;

    @JsonProperty("TruckId")
    private Integer truckId;

    @JsonProperty("ProductId")
    private Integer productId;
}

