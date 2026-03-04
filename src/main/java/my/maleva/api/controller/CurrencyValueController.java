package my.maleva.api.controller;

import my.maleva.api.agentcompany.common.ApiResponse;
import my.maleva.api.dto.CurrencyValueDto;
import my.maleva.api.service.CurrencyValueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;

/**
 * CurrencyValueController - REST Controller for Currency Value API
 * Provides endpoints to retrieve currency values for customers
 */
@RestController
@RequestMapping("/api/currency-value")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_USER')")
public class CurrencyValueController {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyValueController.class);

    @Autowired
    private CurrencyValueService currencyValueService;

    /**
     * Get currency value for a customer
     *
     * Equivalent to: GetCurrencyValue(int Comid, int CustId)
     *
     * REST API: GET /api/currency-value/get?companyId=1&customerId=5
     *
     * This endpoint:
     * - Retrieves currency value from SymbolMaster based on Customer's SymbolRefId
     * - Filters by company and customer
     * - Ensures customer is active (Active != 2)
     * - Returns CurrencyValue and SymbolRefId
     *
     * @param companyId Company Reference ID (required, positive)
     * @param customerId Customer ID (required, positive)
     * @return ResponseEntity containing ApiResponse with CurrencyValueDto
     *         - Success: HTTP 200 with currency data
     *         - Not Found: HTTP 404 if no data found
     *         - Bad Request: HTTP 400 if parameters invalid
     */
    @GetMapping("/get")
    public ResponseEntity<ApiResponse<CurrencyValueDto>> getCurrencyValue(
            @RequestParam(value = "companyId") @NotNull Integer companyId,
            @RequestParam(value = "customerId") @NotNull Integer customerId) {

        logger.info("API called: Get Currency Value - CompanyId: {}, CustomerId: {}",
                   companyId, customerId);

        try {
            // Validate input parameters
            if (companyId == null || companyId <= 0) {
                logger.warn("Invalid companyId provided: {}", companyId);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure(
                                HttpStatus.BAD_REQUEST,
                                "Company ID must be a valid positive integer"
                        ));
            }

            if (customerId == null || customerId <= 0) {
                logger.warn("Invalid customerId provided: {}", customerId);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure(
                                HttpStatus.BAD_REQUEST,
                                "Customer ID must be a valid positive integer"
                        ));
            }

            // Fetch currency value from service
            Optional<CurrencyValueDto> currencyData = currencyValueService.getCurrencyValue(
                    companyId,
                    customerId
            );

            // Check if data found
            if (currencyData.isPresent()) {
                logger.info("Currency value retrieved successfully - CurrencyValue: {}, SymbolRefId: {}",
                           currencyData.get().getCurrencyValue(),
                           currencyData.get().getSymbolRefId());

                return ResponseEntity.ok(
                        ApiResponse.success(
                                "Currency value retrieved successfully",
                                currencyData.get()
                        )
                );
            } else {
                logger.info("No currency value found for CompanyId: {}, CustomerId: {}",
                           companyId, customerId);

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure(
                                HttpStatus.NOT_FOUND,
                                "No currency value found for the specified company and customer"
                        ));
            }

        } catch (Exception e) {
            logger.error("Error occurred while fetching currency value - CompanyId: {}, CustomerId: {}, Error: {}",
                        companyId, customerId, e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "An error occurred while retrieving currency value: " + e.getMessage()
                    ));
        }
    }

    /**
     * Alternative endpoint using path parameters
     * REST API: GET /api/currency-value/company/{companyId}/customer/{customerId}
     *
     * @param companyId Company Reference ID (required, positive)
     * @param customerId Customer ID (required, positive)
     * @return ResponseEntity containing ApiResponse with CurrencyValueDto
     */
    @GetMapping("/company/{companyId}/customer/{customerId}")
    public ResponseEntity<ApiResponse<CurrencyValueDto>> getCurrencyValueByPath(
            @PathVariable @NotNull Integer companyId,
            @PathVariable @NotNull Integer customerId) {

        logger.info("API called: Get Currency Value (Path Params) - CompanyId: {}, CustomerId: {}",
                   companyId, customerId);

        // Delegate to the query parameter method
        return getCurrencyValue(companyId, customerId);
    }
}

