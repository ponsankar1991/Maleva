package my.maleva.api.module.ai.billextraction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.ai.billextraction.dto.BillExtractionResponse;
import my.maleva.api.module.ai.billextraction.service.BillExtractionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Reads a supplier bill with the configured AI provider. Company scope
 * arrives as {@code companyId} or the {@code Comid} header, like the bills API.
 */
@RestController
@RequestMapping("/api/ai/bills")
@RequiredArgsConstructor
@Tag(name = "AI - Bills", description = "Reads supplier bills with the configured AI provider and returns a draft for the Bills form")
public class BillExtractionController {

    private final BillExtractionService service;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Read a supplier bill",
            description = "Uploads a PDF or image (part 'file') and returns supplier, header and line values for review. "
                    + "Optional 'provider' forces a specific AI provider.")
    public ResponseEntity<ApiResponse<BillExtractionResponse>> extract(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid,
            @RequestParam(required = false) String provider) {
        Integer company = companyId != null ? companyId : comid;
        BillExtractionResponse result = service.extract(company, file, provider);
        return ResponseEntity.ok(ApiResponse.success(result, "Bill read - review before saving"));
    }
}
