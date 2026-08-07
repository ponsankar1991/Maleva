package my.maleva.api.module.ceodashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.ceodashboard.dto.DashboardFilterRequestDto;
import my.maleva.api.module.ceodashboard.dto.TopCustomerResponseDto;
import my.maleva.api.module.ceodashboard.dto.DateRangeResponseDto;
import my.maleva.api.module.ceodashboard.service.CeoDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ceo-dashboard")
@RequiredArgsConstructor
@Tag(name = "CEO Dashboard API", description = "Enterprise APIs for Top Customer Protection, Risk Management, and Financial Analytics")
public class CeoDashboardController {

    private final CeoDashboardService ceoDashboardService;

    @Operation(summary = "Get Top 20 SGD Customers (Ship Spares)", description = "Retrieves the highest revenue generating customers billed in SGD for the Ship Spares division.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list of Top Customers",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter parameters provided")
    })
    @PostMapping("/top-20/sgd")
    public ResponseEntity<ApiResponse<List<TopCustomerResponseDto>>> getTop20SgdCustomers(
            @Valid @RequestBody(required = false) DashboardFilterRequestDto filter) {
        return ResponseEntity.ok(ceoDashboardService.getTop20SgdCustomers(filter));
    }

    @Operation(summary = "Get Top 20 USD Customers (Ship Spares)", description = "Retrieves the highest revenue generating customers billed in USD for the Ship Spares division.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list of Top Customers",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter parameters provided")
    })
    @PostMapping("/top-20/usd")
    public ResponseEntity<ApiResponse<List<TopCustomerResponseDto>>> getTop20UsdCustomers(
            @Valid @RequestBody(required = false) DashboardFilterRequestDto filter) {
        return ResponseEntity.ok(ceoDashboardService.getTop20UsdCustomers(filter));
    }

    @Operation(summary = "Get Top 20 RM Customers (Ship Spares)", description = "Retrieves the highest revenue generating customers billed in RM for the Ship Spares division.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list of Top Customers",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter parameters provided")
    })
    @PostMapping("/top-20/rm")
    public ResponseEntity<ApiResponse<List<TopCustomerResponseDto>>> getTop20RmCustomers(
            @Valid @RequestBody(required = false) DashboardFilterRequestDto filter) {
        return ResponseEntity.ok(ceoDashboardService.getTop20RmCustomers(filter));
    }

    @Operation(summary = "Get Top 20 Transport Customers", description = "Retrieves the highest revenue generating customers for the Transport division.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list of Top Customers",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter parameters provided")
    })
    @PostMapping("/top-20/transport")
    public ResponseEntity<ApiResponse<List<TopCustomerResponseDto>>> getTop20TransportCustomers(
            @Valid @RequestBody(required = false) DashboardFilterRequestDto filter) {
        return ResponseEntity.ok(ceoDashboardService.getTop20TransportCustomers(filter));
    }

    @Operation(summary = "Get Top 20 Overall Customers by Revenue", description = "Retrieves the overall highest revenue generating customers across all divisions.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list of Top Customers",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter parameters provided")
    })
    @PostMapping("/top-20/overall-revenue")
    public ResponseEntity<ApiResponse<List<TopCustomerResponseDto>>> getTop20OverallByRevenue(
            @Valid @RequestBody(required = false) DashboardFilterRequestDto filter) {
        return ResponseEntity.ok(ceoDashboardService.getTop20OverallByRevenue(filter));
    }

    @Operation(summary = "Get Top 20 Overall Customers by Jobs", description = "Retrieves the overall highest volume customers (by job count) across all divisions.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved list of Top Customers",
                content = @Content(schema = @Schema(implementation = ApiResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter parameters provided")
    })
    @PostMapping("/top-20/overall-jobs")
    public ResponseEntity<ApiResponse<List<TopCustomerResponseDto>>> getTop20OverallByJobs(
            @Valid @RequestBody(required = false) DashboardFilterRequestDto filter) {
        return ResponseEntity.ok(ceoDashboardService.getTop20OverallByJobs(filter));
    }

    @Operation(summary = "Get Available Date Range", description = "Retrieves the absolute minimum and maximum SaleDates available in the system for the Date Picker.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved date bounds",
                content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<DateRangeResponseDto>> getAvailableDateRange() {
        return ResponseEntity.ok(ceoDashboardService.getAvailableDateRange());
    }
}
