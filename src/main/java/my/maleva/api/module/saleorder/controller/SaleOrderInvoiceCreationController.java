package my.maleva.api.module.saleorder.controller;

import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoiceCreated;
import my.maleva.api.module.saleorder.dto.SaleOrderInvoicePreview;
import my.maleva.api.module.saleorder.service.SaleOrderInvoiceCreationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create Invoice from the sale order screen.
 *
 * <ul>
 *   <li>{@code GET  /api/sale-orders/{id}/invoice-preview?companyId=} — the
 *       invoice as it would be saved, or the invoice that already exists.</li>
 *   <li>{@code POST /api/sale-orders/{id}/create-invoice?companyId=} — saves
 *       it and moves the job to JOB COMPLET.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/sale-orders")
@PermitAll
@RequiredArgsConstructor
public class SaleOrderInvoiceCreationController {

    private final SaleOrderInvoiceCreationService service;

    @GetMapping("/{id}/invoice-preview")
    public ResponseEntity<ApiResponse<SaleOrderInvoicePreview>> preview(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        SaleOrderInvoicePreview preview = service.preview(id, companyId);
        String message = preview.existing() != null
                ? "Invoice " + preview.existing().invoiceNo() + " already exists for this job"
                : preview.canCreate() ? "Ready to create" : String.join("; ", preview.blockers());
        return ResponseEntity.ok(ApiResponse.success(preview, message));
    }

    @PostMapping("/{id}/create-invoice")
    public ResponseEntity<ApiResponse<SaleOrderInvoiceCreated>> create(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        SaleOrderInvoiceCreated created = service.create(id, companyId);
        return ResponseEntity.ok(ApiResponse.success(created,
                "Invoice " + created.invoiceNo() + " created. " + created.statusMessage()));
    }
}
