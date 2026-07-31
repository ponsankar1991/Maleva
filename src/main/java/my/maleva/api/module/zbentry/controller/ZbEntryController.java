package my.maleva.api.module.zbentry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.zbentry.dto.ZbEntryResponse;
import my.maleva.api.module.zbentry.dto.ZbEntrySearchRequest;
import my.maleva.api.module.zbentry.service.ZbEntryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
}
