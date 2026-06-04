package my.maleva.api.module.master.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.master.dto.BankMasterDto;
import my.maleva.api.common.dto.ComboListModel;
import my.maleva.api.module.master.service.BankMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/banks")
@Validated
@PermitAll
public class BankMasterController {

    private static final Logger logger = LoggerFactory.getLogger(BankMasterController.class);
    private final BankMasterService service;

    public BankMasterController(BankMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<BankMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public BankMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    /**
     * Get active banks for a company as combo list
     * Equivalent to .NET: GetBank(int Comid)
     *
     * GET /api/banks/company/{companyRefId}
     *
     * @param companyRefId Company ID
     * @return List of banks with Id and AccountName
     */
    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<ComboListModel>> getBank(@PathVariable Integer companyRefId) {
        logger.info("GetBank API called for company: {}", companyRefId);
        try {
            List<ComboListModel> banks = service.getBank(companyRefId);
            logger.info("Returning {} banks for company: {}", banks.size(), companyRefId);
            return ResponseEntity.ok(banks);
        } catch (Exception ex) {
            logger.error("Error in getBank endpoint for company: {}", companyRefId, ex);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<BankMasterDto> create(@Valid @RequestBody BankMasterDto dto) {
        BankMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/banks/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public BankMasterDto update(@PathVariable Integer id, @Valid @RequestBody BankMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
