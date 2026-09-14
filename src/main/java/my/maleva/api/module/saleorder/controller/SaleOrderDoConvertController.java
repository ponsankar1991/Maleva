package my.maleva.api.module.saleorder.controller;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.invoice.print.PrintStash;
import my.maleva.api.module.saleorder.dto.DoConvertResult;
import my.maleva.api.module.saleorder.print.DeliveryOrderPdfService;
import my.maleva.api.module.saleorder.service.SaleOrderDoConvertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Prepare a delivery order from a sale order.
 * {@code POST /api/sale-orders/{id}/do-convert?companyId=1}
 *
 * <p>Replaces the legacy {@code /SaleOrder/DoConvert}. A DO that could not
 * be prepared answers 200 with {@code IsSuccess=false} and the reason, the
 * same mixed signal the other action endpoints use, so the screen shows the
 * message instead of a generic failure.
 */
@RestController
@RequestMapping("/api/sale-orders")
@PermitAll
@RequiredArgsConstructor
public class SaleOrderDoConvertController {

    private static final Logger log = LoggerFactory.getLogger(SaleOrderDoConvertController.class);

    private final SaleOrderDoConvertService doConvertService;
    private final DeliveryOrderPdfService deliveryOrderPdfService;
    private final PrintStash printStash;

    @PostMapping("/{id}/do-convert")
    public ResponseEntity<ApiResponse<DoConvertResult>> doConvert(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        DoConvertResult result = doConvertService.convert(id, companyId);
        if (!result.ok()) {
            ApiResponse<DoConvertResult> body = ApiResponse.error(result.message(), 200);
            body.setData1(result);
            return ResponseEntity.ok(body);
        }
        return ResponseEntity.ok(ApiResponse.success(result, result.message()));
    }

    /**
     * Prepare the DO and hand back a link to its printed report.
     * {@code POST /api/sale-orders/{id}/do-convert/print-ticket?companyId=1}
     *
     * <p>The legacy DO button did both in one go: {@code /SaleOrder/DoConvert},
     * then {@code ReportViewer.aspx?ReportName=DoReport} in a new window. This
     * call prepares the DO exactly as {@link #doConvert} does, renders the PDF,
     * and parks it in the shared {@link PrintStash}. The window opens the
     * returned {@code Url}, which needs no bearer token and ends in the DO
     * number, so the browser offers {@code DO000001234.pdf}.
     *
     * <p>A DO that could not be prepared answers 200 with {@code IsSuccess=false}
     * and the reason, like {@link #doConvert}; a DO that was prepared but could
     * not be rendered answers 500 with the reason, and stays prepared.
     */
    @PostMapping("/{id}/do-convert/print-ticket")
    public ResponseEntity<ApiResponse<Map<String, Object>>> doConvertPrintTicket(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        DoConvertResult result = doConvertService.convert(id, companyId);
        if (!result.ok()) {
            return ResponseEntity.ok(ApiResponse.error(result.message(), 200));
        }
        try {
            DeliveryOrderPdfService.RenderedDeliveryOrder rendered = deliveryOrderPdfService.render(result);
            String ticket = printStash.put(rendered.fileName(), rendered.pdf());

            Map<String, Object> body = new HashMap<>();
            body.put("DoId", result.doId());
            body.put("DoNo", result.doNo());
            body.put("Ticket", ticket);
            body.put("FileName", rendered.fileName());
            // The collect endpoint lives with the invoice print and reads the same stash.
            body.put("Url", "/api/v1/sale-invoices/print/" + ticket + "/" + rendered.fileName());
            return ResponseEntity.ok(ApiResponse.success(body, result.message()));
        } catch (RuntimeException e) {
            log.error("DO {} for sale order {} (company {}) was prepared but could not be printed",
                    result.doNo(), id, companyId, e);
            Throwable root = e;
            while (root.getCause() != null && root.getCause() != root) {
                root = root.getCause();
            }
            String reason = root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("DO " + result.doNo() + " was prepared but could not be printed: " + reason,
                            HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
