package my.maleva.api.module.saleorderforwardingreport.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.agentcompany.common.ApiResponse;
import my.maleva.api.module.saleorderforwardingreport.dto.ExcelImportResultDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingDateUpdateRequest;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingReportRowDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingReportSearchRequest;
import my.maleva.api.module.saleorderforwardingreport.dto.ForwardingS1OptionsDto;
import my.maleva.api.module.saleorderforwardingreport.dto.ZbReportRowDto;
import my.maleva.api.module.saleorderforwardingreport.service.ForwardingReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * The sale order forwarding report — legacy `Report/SaleOrderFWReport`.
 *
 * <p>Replaces six legacy actions that were split across two controllers
 * (`TransactionReport` and `SaleOrder`) purely by accident of history; they all
 * belong to this one screen, so they live together here.
 *
 * <p>One legacy action has deliberately <b>no</b> equivalent:
 * `TransactionReport/SelectSaleOrderFWExcel`, which ran the same query as the
 * grid, wrote an .xlsx into a folder on the web server, and returned a URL for
 * the browser to navigate to. The rows are identical to
 * {@link #searchForwarding}'s, so the screen exports the rows it already holds
 * and no server-side file, path or cleanup is involved.
 */
@Slf4j
@RestController
@RequestMapping("/api/sale-order-forwarding-report")
@RequiredArgsConstructor
public class ForwardingReportController {

    private final ForwardingReportService service;

    /** The forwarding grid: one row per populated leg of each matching order. */
    @PostMapping("/forwarding/search")
    public ResponseEntity<ApiResponse<List<ForwardingReportRowDto>>> searchForwarding(
            @RequestBody ForwardingReportSearchRequest request) {

        log.info("POST /api/sale-order-forwarding-report/forwarding/search comId={} {}..{}",
                request.getComId(), request.getFromDate(), request.getToDate());

        List<ForwardingReportRowDto> rows = service.searchForwarding(request);
        return ResponseEntity.ok(ApiResponse.success("Forwarding rows fetched successfully", rows));
    }

    /** The ZB grid: one row per matching order. */
    @PostMapping("/zb/search")
    public ResponseEntity<ApiResponse<List<ZbReportRowDto>>> searchZb(
            @RequestBody ForwardingReportSearchRequest request) {

        log.info("POST /api/sale-order-forwarding-report/zb/search comId={} {}..{}",
                request.getComId(), request.getFromDate(), request.getToDate());

        List<ZbReportRowDto> rows = service.searchZb(request);
        return ResponseEntity.ok(ApiResponse.success("ZB rows fetched successfully", rows));
    }

    /** Options for the six S1/S2 filter dropdowns. */
    @GetMapping("/s1-options/{comId}")
    public ResponseEntity<ApiResponse<ForwardingS1OptionsDto>> getS1Options(
            @PathVariable Integer comId) {

        return ResponseEntity.ok(ApiResponse.success(
                "S1 options fetched successfully", service.getS1Options(comId)));
    }

    /** Vessel names seen on either side of the company's orders. */
    @GetMapping("/vessel-names/{comId}")
    public ResponseEntity<ApiResponse<List<String>>> getVesselNames(@PathVariable Integer comId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Vessel names fetched successfully", service.getVesselNames(comId)));
    }

    /** Re-date one forwarding leg of one sale order. */
    @PutMapping("/forwarding-date")
    public ResponseEntity<ApiResponse<Boolean>> updateForwardingDate(
            @Valid @RequestBody ForwardingDateUpdateRequest request) {

        log.info("PUT /api/sale-order-forwarding-report/forwarding-date jobId={} fwNo={}",
                request.getJobId(), request.getFwNo());

        boolean updated = service.updateForwardingDate(request);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error("No sale order " + request.getJobId() + " for this company"));
        }
        return ResponseEntity.ok(ApiResponse.success("Forwarding date updated successfully", true));
    }

    /**
     * Apply a customs acknowledgement spreadsheet.
     *
     * <p>Always answers 200 with a per-row breakdown when the file could be
     * read, even if nothing matched — "0 updated, 412 skipped" is a result the
     * operator needs to see, not an error. A 400 means the file itself was
     * unusable: wrong type, empty, or too narrow to be an acknowledgement sheet.
     */
    @PostMapping("/excel-import")
    public ResponseEntity<ApiResponse<ExcelImportResultDto>> importExcel(
            @RequestParam("comId") Integer comId,
            @RequestParam("file") MultipartFile file) {

        log.info("POST /api/sale-order-forwarding-report/excel-import comId={} file={}",
                comId, file == null ? null : file.getOriginalFilename());

        try {
            ExcelImportResultDto result = service.importExcel(comId, file);
            return ResponseEntity.ok(ApiResponse.success(summarise(result), result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Forwarding Excel import failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    private String summarise(ExcelImportResultDto result) {
        return "%d of %d rows updated, %d skipped, %d failed".formatted(
                result.getUpdatedCount(),
                result.getTotalRows(),
                result.getSkippedCount(),
                result.getFailedCount());
    }
}
