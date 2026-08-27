package my.maleva.api.module.billing.bill.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.integration.qne.QnePushResponses;
import my.maleva.api.module.billing.bill.dto.BillMasterDto;
import my.maleva.api.module.billing.bill.service.BillMasterService;
import my.maleva.api.module.billing.bill.service.BillQneService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bills")
@Validated
@PermitAll
public class BillMasterController {

    private final BillMasterService service;
    private final BillQneService qneService;

    public BillMasterController(BillMasterService service, BillQneService qneService) {
        this.service = service;
        this.qneService = qneService;
    }

    /**
     * Push bill to QNE
     * POST /api/bills/{id}/push-qne?companyId=1
     *
     * Create-once via the empty-QNECode guard (legacy BillMasterConvert). A
     * QNE rejection answers 200 with IsSuccess=false and QNE's own message.
     */
    @PostMapping("/{id}/push-qne")
    public ResponseEntity<ApiResponse<Map<String, Object>>> pushToQne(
            @PathVariable Integer id,
            @RequestParam Integer companyId) {
        if (id == null || id <= 0 || companyId == null || companyId <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid ID or company ID", 400));
        }
        return QnePushResponses.toResponse(qneService.push(id, companyId));
    }

    @GetMapping
    public List<BillMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public BillMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<BillMasterDto> create(@Valid @RequestBody BillMasterDto dto) {
        BillMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/bills/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public BillMasterDto update(@PathVariable Integer id, @Valid @RequestBody BillMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
