package my.maleva.api.module.patmentvouchmaster.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ResponseViewModel;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherDto;
import my.maleva.api.module.patmentvouchmaster.service.PaymentVoucherService;
import my.maleva.api.module.accounting.dto.COAExpenseResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payment-vouchers")
@Validated
@PermitAll
public class PaymentVoucherController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentVoucherController.class);

    private final PaymentVoucherService service;

    public PaymentVoucherController(PaymentVoucherService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentVoucherDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PaymentVoucherDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PaymentVoucherDto> create(@Valid @RequestBody PaymentVoucherDto dto) {
        PaymentVoucherDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/payment-vouchers/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PaymentVoucherDto update(@PathVariable Integer id, @Valid @RequestBody PaymentVoucherDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/select-coa-expense")
    public ResponseEntity<?> selectCOAExpense(
            @RequestParam(value = "comid", required = false) Integer comid,
            @RequestParam(value = "expenseId", required = false) Integer expenseId,
            @RequestParam(value = "keyword", required = false) String keyword) {

        logger.info("API Call: selectCOAExpense - comid: {}, expenseId: {}, keyword: {}", comid, expenseId, keyword);
        try {
            // Validate required parameters
            if (comid == null || comid <= 0) {
                logger.warn("Invalid request: comid is missing or invalid");
                return ResponseEntity.badRequest()
                        .body(new Object() {
                            public final boolean ok = false;
                            public final String message = "Company ID (comid) is required and must be greater than 0";
                        });
            }

            // Set default value for expenseId
            if (expenseId == null) {
                expenseId = 0;
            }

            // Call service to fetch COA Expense details
            List<COAExpenseResponseDto> resultList = service.selectCOAExpense(comid, expenseId, keyword);

            logger.info("Successfully retrieved {} COA Expense records", resultList.size());

            // Return success response with data
            return ResponseEntity.ok(new Object() {
                public final boolean ok = true;
                public final List<COAExpenseResponseDto> data = resultList;
            });

        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid request: {}", ex.getMessage());
            return ResponseEntity.badRequest()
                    .body(new Object() {
                        public final boolean ok = false;
                        public final String message = ex.getMessage();
                    });

        } catch (Exception ex) {
            logger.error("Error in selectCOAExpense endpoint", ex);
            return ResponseEntity.internalServerError()
                    .body(new Object() {
                        public final boolean ok = false;
                        public final String message = "Error retrieving COA Expense details: " +
                                                      (ex.getCause() != null ?
                                                       ex.getCause().getMessage() : ex.getMessage());
                    });
        }
    }
}
