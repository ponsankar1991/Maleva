package my.maleva.api.module.zbentry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.zbentry.dto.ZbEntryBulkSaveRequest;
import my.maleva.api.module.zbentry.dto.ZbEntryResponse;
import my.maleva.api.module.zbentry.dto.ZbEntrySaveResult;
import my.maleva.api.module.zbentry.dto.ZbEntrySearchRequest;
import my.maleva.api.module.zbentry.service.ZbEntryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zb-entries")
@RequiredArgsConstructor
@Tag(name = "ZbEntry API", description = "Endpoints for managing ZbEntry (Billing Master) searches")
public class ZbEntryController {

    private final ZbEntryService zbEntryService;

    @GetMapping
    @Operation(summary = "Search ZbEntries", description = "Paginated and filtered search for ZbEntries. companyRefId, fromDate, and toDate are required.")

    @PermitAll
     public ResponseEntity<ApiResponse<Page<ZbEntryResponse>>> searchZbEntries(@Valid ZbEntrySearchRequest request, Pageable pageable) {
        Page<ZbEntryResponse> responsePage = zbEntryService.searchZbEntries(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(responsePage, "ZbEntry search completed successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one ZbEntry",
            description = "Loads a single entry for the form. Scoped to the company, so an id "
                    + "from another company reads as not found.")
    @PermitAll
    public ResponseEntity<ApiResponse<ZbEntryResponse>> getZbEntry(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId) {

        ZbEntryResponse entry = zbEntryService.getZbEntry(id, companyRefId);
        return ResponseEntity.ok(ApiResponse.success(entry, "ZbEntry loaded successfully"));
    }

    @PostMapping("/bulk-save")
    @Operation(summary = "Save ZbEntries",
            description = "Upserts rows through SP_ZBEntryMaster. A row with id 0 or null is "
                    + "inserted, anything else updated; the whole batch is one transaction. The "
                    + "response carries the saved row's id so a new entry can have attachments "
                    + "filed against it.")
    @PermitAll
    public ResponseEntity<ApiResponse<ZbEntrySaveResult>> bulkSaveZbEntries(
            @RequestBody @Valid ZbEntryBulkSaveRequest request) {

        ZbEntrySaveResult result = zbEntryService.bulkSaveZbEntries(request);

        // The procedure reports its own failure and has already rolled back, so
        // surface its message rather than a generic 500.
        if (!result.isSuccess()) {
            return ResponseEntity.unprocessableEntity().body(
                    ApiResponse.error(result.getMsg(), HttpStatus.UNPROCESSABLE_ENTITY.value()));
        }

        return ResponseEntity.ok(ApiResponse.success(result, result.getMsg()));
    }
}
