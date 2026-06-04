package my.maleva.api.module.accounting.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.agentcompany.common.ApiResponse;
import my.maleva.api.module.accounting.dto.CurrencyValueDto;
import my.maleva.api.module.accounting.service.CurrencyValueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * CurrencyValueController - REST Controller for Currency Value API
 * Provides endpoints to retrieve currency values for customers
 */
@RestController
@RequestMapping("/api/currency-value")
@PermitAll
public class CurrencyValueController {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyValueController.class);

    @Autowired
    private CurrencyValueService currencyValueService;


    @GetMapping("/get")
    public ResponseEntity<ApiResponse<CurrencyValueDto>> getCurrencyValue(
            @RequestParam Integer companyId,
            @RequestParam Integer customerId) {

        logger.info("Get Currency Value API called - companyId: {}, customerId: {}", companyId, customerId);

        try {

            Optional<CurrencyValueDto> currencyValue =
                    currencyValueService.getCurrencyValue(companyId, customerId);

            if (currencyValue.isPresent()) {

                return ResponseEntity.ok(
                        ApiResponse.success(
                                "Currency value retrieved successfully",
                                currencyValue.get()
                        )
                );

            } else {

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.failure(
                                HttpStatus.NOT_FOUND,
                                "Currency value not found"
                        ));
            }

        } catch (Exception e) {

            logger.error("Error while fetching currency value", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Internal server error"
                    ));
        }
    }


}

