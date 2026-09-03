package my.maleva.api.module.ai.purchaseorder.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.ai.purchaseorder.dto.PurchaseOrderExtractionResponse;
import my.maleva.api.module.ai.purchaseorder.service.PurchaseOrderExtractionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Reads a supplier quotation / invoice / DO into a purchase-order draft.
 * Company scope arrives as {@code companyId} or the {@code Comid} header,
 * like the purchase-order API itself.
 */
@RestController
@RequestMapping("/api/ai/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "AI - Purchase Orders", description = "Reads supplier documents with the configured AI provider and returns a draft for the Purchase Order form")
public class PurchaseOrderExtractionController {

    private final PurchaseOrderExtractionService service;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Read a supplier document into a purchase order",
            description = "Uploads a PDF or image (part 'file') and returns supplier, header, truck, driver and line values for review. "
                    + "Optional 'provider' forces a specific AI provider.")
    public ResponseEntity<ApiResponse<PurchaseOrderExtractionResponse>> extract(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid,
            @RequestParam(required = false) String provider) {
        Integer company = companyId != null ? companyId : comid;
        PurchaseOrderExtractionResponse result = service.extract(company, file, provider);
        return ResponseEntity.ok(ApiResponse.success(result, "Document read - review before saving"));
    }
}
