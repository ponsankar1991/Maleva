package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.fleet.dto.TollEntryDetailDto;
import my.maleva.api.module.fleet.dto.TollEntryListResponse;
import my.maleva.api.module.fleet.dto.request.TollEntrySaveRequest;
import my.maleva.api.module.fleet.dto.request.TollEntrySearchRequest;
import my.maleva.api.module.fleet.service.TollEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * Toll entries.
 *
 * Replaces the legacy /TollEntry/* MVC actions. Reads are GETs with query
 * parameters rather than POSTs with a JSON body, and the company id travels as
 * a parameter instead of the {@code Comid} header.
 */
@RestController
@RequestMapping("/api/toll-entries")
@Validated
@PermitAll
public class TollEntryController {

    private static final Logger logger = LoggerFactory.getLogger(TollEntryController.class);

    private final TollEntryService service;

    public TollEntryController(TollEntryService service) {
        this.service = service;
    }

    /**
     * The toll entry list with its total.
     * Legacy equivalent: POST /TollEntry/SelectTollEntry
     */
    @GetMapping
    public ResponseEntity<ApiResponse<TollEntryListResponse>> search(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer truckRefId,
            @RequestParam(required = false) Integer employeeRefId,
            @RequestParam(required = false) String search) {

        TollEntryListResponse data = service.search(TollEntrySearchRequest.builder()
                .companyRefId(companyRefId)
                .fromDate(fromDate)
                .toDate(toDate)
                .truckRefId(truckRefId)
                .employeeRefId(employeeRefId)
                .search(search)
                .build());

        return ResponseEntity.ok(ApiResponse.success(data, "Toll entries retrieved"));
    }

    /**
     * The next toll number to show on a blank form.
     * Legacy equivalent: POST /TollEntry/MaxTollEntryNo
     */
    @GetMapping("/next-no")
    public ResponseEntity<ApiResponse<String>> nextTollNumber(@RequestParam Integer companyRefId) {
        return ResponseEntity.ok(
                ApiResponse.success(service.nextTollNumber(companyRefId), "Next toll number"));
    }

    /**
     * One entry with its transactions, for the edit form.
     * Legacy equivalent: POST /TollEntry/EditTollEntry
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TollEntryDetailDto>> getForEdit(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) Integer tollNumber) {

        return ResponseEntity.ok(
                ApiResponse.success(service.getForEdit(id, tollNumber, companyRefId),
                        "Toll entry retrieved"));
    }

    /**
     * The data behind the print action. Rendering still belongs to the .NET
     * report server, as it does for RTI and Planning.
     * Legacy equivalent: POST /TollEntry/TollEntryVIEW
     */
    @GetMapping("/{id}/print-data")
    public ResponseEntity<ApiResponse<TollEntryDetailDto>> getForPrint(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId) {

        return ResponseEntity.ok(
                ApiResponse.success(service.getForPrint(id, companyRefId), "Toll entry print data"));
    }

    /**
     * Creates or updates an entry and all of its transactions.
     * Legacy equivalent: POST /TollEntry/InsertTollEntry
     *
     * <p>The whole detail set is replaced, so send every line, not just the
     * changed ones. The header Amount is recomputed from them.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TollEntryDetailDto>> save(
            @Valid @RequestBody TollEntrySaveRequest request,
            Authentication authentication) {

        TollEntryDetailDto saved = service.save(request, usernameOf(authentication));
        logger.info("Saved toll entry {} with {} transactions",
                saved.getId(), saved.getDetails() == null ? 0 : saved.getDetails().size());
        return ResponseEntity.ok(ApiResponse.success(saved, "Toll entry saved"));
    }

    /**
     * Soft delete.
     * Legacy equivalent: POST /TollEntry/DeleteTollEntry
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId,
            Authentication authentication) {

        service.delete(id, companyRefId, usernameOf(authentication));
        return ResponseEntity.ok(ApiResponse.success(null, "Toll entry deleted"));
    }

    private String usernameOf(Authentication authentication) {
        return authentication == null ? "system" : authentication.getName();
    }
}
