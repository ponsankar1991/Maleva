package my.maleva.api.module.payment.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.payment.service.PaymentQneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST entry point for the Payment entity's QNE flow. Payment CRUD still
 * lives in the legacy screens; this controller exists so the pay-bill push
 * (legacy PaymentConvert) has a Java home.
 */
@RestController
@RequestMapping("/api/payments")
@PermitAll
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentQneService qneService;

    public PaymentController(PaymentQneService qneService) {
        this.qneService = qneService;
    }

    /**
     * Push payment to QNE PayBills
     * POST /api/payments/{id}/push-qne?companyId=1
     *
     * Create-once via the empty-QNECode guard (legacy PaymentConvert). A QNE
     * rejection answers 200 with IsSuccess=false and QNE's own message.
     */
    @PostMapping("/{id}/push-qne")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pushToQne(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        logger.info("Pushing payment ID: {} to QNE for company: {}", id, companyId);
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid ID or company ID", 400));
        }
        return QnePushResponses.toResponse(qneService.push(id, companyId));
    }
}
