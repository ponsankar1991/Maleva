package my.maleva.api.module.pettycash.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.pettycash.dto.PettyCashEditDto;
import my.maleva.api.module.pettycash.dto.PettyCashF5ViewDto;
import my.maleva.api.module.pettycash.dto.PettyCashMasterDto;
import my.maleva.api.module.pettycash.dto.PettyCashSaveRequestDto;
import my.maleva.api.module.pettycash.dto.PettyCashSaveResponseDto;
import my.maleva.api.module.pettycash.dto.SelectPettyCashRequestDto;
import my.maleva.api.module.pettycash.service.PettyCashMasterService;
import my.maleva.api.module.pettycash.service.PettyCashTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/petty-cash-masters")
@Validated
@PermitAll
public class PettyCashMasterController {

    private static final Logger logger = LoggerFactory.getLogger(PettyCashMasterController.class);

    private final PettyCashMasterService service;
    private final PettyCashTransactionService transactions;

    public PettyCashMasterController(PettyCashMasterService service,
                                      PettyCashTransactionService transactions) {
        this.service = service;
        this.transactions = transactions;
    }

    /* ── petty cash screen ────────────────────────────────────────────── */

    /**
     * Next petty cash number, for a blank screen.
     * GET /api/petty-cash-masters/next-number?companyId=6
     *
     * <p>Preview only — the number is assigned when the record is saved.
     */
    @GetMapping("/next-number")
    public ResponseEntity<ApiResponse<String>> nextNumber(@RequestParam Integer companyId) {
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.nextPettyCashNumber(companyId), "Next petty cash number generated"));
        } catch (Exception e) {
            logger.error("Error generating next petty cash number for company {}", companyId, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error generating petty cash number: " + e.getMessage(), 500));
        }
    }

    /**
     * Save a petty cash record and its lines — insert when id is 0/absent,
     * otherwise update.
     * POST /api/petty-cash-masters/save?companyId=6
     */
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<PettyCashSaveResponseDto>> save(
            @RequestBody PettyCashSaveRequestDto dto,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        Integer company = companyId != null ? companyId : comid;
        if (company == null || company <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Company ID is required", 400));
        }
        try {
            PettyCashSaveResponseDto result = transactions.save(dto, company);
            if (!result.isSuccess()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(result.getMessage(), 400));
            }
            return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
        } catch (Exception e) {
            logger.error("Error saving petty cash for company {}", company, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error saving petty cash: " + e.getMessage(), 500));
        }
    }

    /**
     * Load one petty cash record for editing, by id.
     * GET /api/petty-cash-masters/edit?companyId=6&id=12
     */
    @GetMapping("/edit")
    public ResponseEntity<ApiResponse<PettyCashEditDto>> edit(
            @RequestParam Integer companyId,
            @RequestParam(required = false) Integer id) {
        if (id == null || id == 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Provide id", 400));
        }
        try {
            return transactions.edit(id, companyId)
                    .map(dto -> ResponseEntity.ok(ApiResponse.success(dto, "Petty cash loaded")))
                    .orElseGet(() -> ResponseEntity.status(404)
                            .body(ApiResponse.error("Petty cash not found: " + id, 404)));
        } catch (Exception e) {
            logger.error("Error loading petty cash id={}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error loading petty cash: " + e.getMessage(), 500));
        }
    }

    /**
     * The F5 search grid: petty cash records plus their lines and the total.
     * POST /api/petty-cash-masters/search?companyId=6
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PettyCashF5ViewDto>> search(
            @RequestBody SelectPettyCashRequestDto request,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        Integer company = companyId != null ? companyId
                : comid != null ? comid : request.getComid();
        if (company == null || company <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Company ID is required", 400));
        }
        try {
            return ResponseEntity.ok(ApiResponse.success(
                    transactions.search(request, company), "Success"));
        } catch (Exception e) {
            logger.error("Error searching petty cash for company {}", company, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error searching petty cash: " + e.getMessage(), 500));
        }
    }

    @GetMapping
    public List<PettyCashMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PettyCashMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PettyCashMasterDto> create(@Valid @RequestBody PettyCashMasterDto dto) {
        PettyCashMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/petty-cash-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PettyCashMasterDto update(@PathVariable Integer id, @Valid @RequestBody PettyCashMasterDto dto) {
        return service.update(id, dto);
    }

    /**
     * Soft-delete a petty cash record ({@code Active=2}), scoped to the
     * caller's company.
     *
     * <p>This replaces an earlier hard delete that removed the row outright
     * and checked no company — it could delete another company's record by id
     * and stranded the {@code PettyCashDetail} rows beneath it, which are
     * joined by a plain FK with no cascade. {@code companyId} is now required.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer companyId,
            @RequestHeader(value = "Comid", required = false) Integer comid) {
        Integer company = companyId != null ? companyId : comid;
        if (company == null || company <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Company ID is required", 400));
        }
        try {
            if (!transactions.delete(id, company)) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("Petty cash not found: " + id, 404));
            }
            return ResponseEntity.ok(ApiResponse.success(null, "Petty cash deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting petty cash {}", id, e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Error deleting petty cash: " + e.getMessage(), 500));
        }
    }
}
