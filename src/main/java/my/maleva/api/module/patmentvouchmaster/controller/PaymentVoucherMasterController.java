package my.maleva.api.module.patmentvouchmaster.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherMasterDto;
import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherComboResponse;
import my.maleva.api.module.patmentvouchmaster.service.PaymentVoucherMasterService;
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
@RequestMapping("/api/payment-voucher-masters")
@Validated
@PermitAll
public class PaymentVoucherMasterController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentVoucherMasterController.class);

    private final PaymentVoucherMasterService service;

    public PaymentVoucherMasterController(PaymentVoucherMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentVoucherMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PaymentVoucherMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PaymentVoucherMasterDto> create(@Valid @RequestBody PaymentVoucherMasterDto dto) {
        PaymentVoucherMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/payment-voucher-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PaymentVoucherMasterDto update(@PathVariable Integer id, @Valid @RequestBody PaymentVoucherMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * SelectPaymentTo - Get distinct PayTo values for a company
     * Equivalent to .NET SelectPaymentTo method from PaymentVoucherServices
     * HTTP: GET /api/payment-voucher-masters/select-payment-to
     * Header: Comid (Company ID) OR Query Parameter: ?comid=6
     * Response: { "ok": true/false, "message": "...", "data": [...] }
     * Example: GET /api/payment-voucher-masters/select-payment-to?comid=6
     * @param comid Company ID passed as request parameter
     * @return PaymentVoucherComboResponse with distinct PayTo values
     */
    @GetMapping("/select-payment-to")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<PaymentVoucherComboResponse> selectPaymentTo(
            @RequestParam(value = "comid", required = false) Integer comid) {

        logger.info("SelectPaymentTo endpoint called - comid: {}", comid);

        try {
            if (comid == null || comid <= 0) {
                logger.warn("Invalid request: Comid is missing or invalid: {}", comid);
                PaymentVoucherComboResponse response = PaymentVoucherComboResponse.error("Company ID (Comid) is required");
                return ResponseEntity.badRequest().body(response);
            }

            logger.info("Calling service for company: {}", comid);
            PaymentVoucherComboResponse response = service.selectPaymentTo(comid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in SelectPaymentTo endpoint", e);
            PaymentVoucherComboResponse response = PaymentVoucherComboResponse.error("Error retrieving PayTo values: " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * SelectPaymentFrom - Get distinct PayFrom values for a company
     * Equivalent to .NET SelectPaymentFrom method from PaymentVoucherServices
     * HTTP: POST /api/payment-voucher-masters/select-payment-from
     * Header: Comid (Company ID) OR Query Parameter: ?comid=6
     * Response: { "ok": true/false, "message": "...", "data": [...] }
     * Example: POST /api/payment-voucher-masters/select-payment-from?comid=6
     * @param comid Company ID passed as request parameter
     * @return PaymentVoucherComboResponse with distinct PayFrom values
     */
    @GetMapping("/select-payment-from")
    @PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
    public ResponseEntity<PaymentVoucherComboResponse> selectPaymentFrom(
            @RequestParam(value = "comid", required = false) Integer comid) {

        logger.info("SelectPaymentFrom endpoint called - comid: {}", comid);

        try {
            if (comid == null || comid <= 0) {
                logger.warn("Invalid request: Comid is missing or invalid: {}", comid);
                PaymentVoucherComboResponse response = PaymentVoucherComboResponse.error("Company ID (Comid) is required");
                return ResponseEntity.badRequest().body(response);
            }
            logger.info("Calling service for company: {}", comid);
            PaymentVoucherComboResponse response = service.selectPaymentFrom(comid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error in SelectPaymentFrom endpoint", e);
            PaymentVoucherComboResponse response = PaymentVoucherComboResponse.error("Error retrieving PayFrom values: " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
