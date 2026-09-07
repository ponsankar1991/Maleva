package my.maleva.api.module.saleorder.controller;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceLink;
import my.maleva.api.module.saleorder.service.SaleOrderInvoiceLinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Has this sale order been invoiced already?
 * {@code GET /api/sale-orders/{id}/invoice-link?companyId=1}
 *
 * <p>Replaces the legacy {@code /SaleOrder/SelectInvoiceNumber}, which the
 * Push Invoice button called with a synchronous XHR that froze the browser.
 * The answer names the invoice, so the screen can offer to open it rather
 * than only saying one exists.
 */
@RestController
@RequestMapping("/api/sale-orders")
@PermitAll
@RequiredArgsConstructor
public class SaleOrderInvoiceLinkController {

    private final SaleOrderInvoiceLinkService invoiceLinkService;

    @GetMapping("/{id}/invoice-link")
    public ResponseEntity<ApiResponse<SaleOrderInvoiceLink>> invoiceLink(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        return invoiceLinkService.find(id, companyId)
                .map(link -> ResponseEntity.ok(ApiResponse.success(link,
                        link.invoiced()
                                ? "Invoice " + link.invoiceNo() + " already exists for this job"
                                : "This job has not been invoiced")))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Sale order " + id + " was not found for this company", 404)));
    }
}
