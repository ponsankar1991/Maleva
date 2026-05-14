package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for SelectPurchaseMaster operation
 * Contains all filter parameters for the purchase master report query
 * Equivalent to request parameters in .NET SelectPurchaseMaster method
 *
 * <h3>Filter Logic:</h3>
 * <ul>
 *   <li>supplierId (Id) > 0: Filter by supplier</li>
 *   <li>employeeId (Employeeid) > 0: Filter by employee</li>
 *   <li>driverId (DriverId) > 0: Filter by driver</li>
 *   <li>truckId (TruckId) > 0: Filter by truck</li>
 *   <li>productId (ProductId) > 0: Filter by product in purchase details</li>
 *   <li>search: Filter by CNumberDisplay or InvoiceNo (overrides date filter)</li>
 *   <li>invoiceCheck = 1: Filter by InvoiceDate, 0: Filter by SaleDate</li>
 * </ul>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectPurchaseMasterRequestDto {

    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    @JsonProperty("Comid")
    private Integer companyId;

    @NotBlank(message = "From Date is required")
    @JsonProperty("Fromdate")
    private String fromDate;

    @NotBlank(message = "To Date is required")
    @JsonProperty("Todate")
    private String toDate;

    @JsonProperty("Id")
    @Builder.Default
    private Integer supplierId = 0;

    @JsonProperty("Employeeid")
    @Builder.Default
    private Integer employeeId = 0;

    @JsonProperty("Search")
    private String search;

    @JsonProperty("invoicecheck")
    @Builder.Default
    private Integer invoiceCheck = 0;

    @JsonProperty("DriverId")
    @Builder.Default
    private Integer driverId = 0;

    @JsonProperty("TruckId")
    @Builder.Default
    private Integer truckId = 0;

    @JsonProperty("ProductId")
    @Builder.Default
    private Integer productId = 0;
}

