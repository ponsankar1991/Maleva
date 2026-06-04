package my.maleva.api.module.master.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.master.dto.AddressMasterDto;
import my.maleva.api.module.master.service.AddressMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@Validated
public class AddressMasterController {

    private final AddressMasterService service;

    public AddressMasterController(AddressMasterService service) {
        this.service = service;
    }

    /**
     * Get all addresses
     */
    @GetMapping
    @PermitAll

    public List<AddressMasterDto> list() {
        return service.listAll();
    }

    /**
     * Get address by ID
     */
    @GetMapping("/{id}")

    @PermitAll
    public AddressMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    /**
     * Create new address
     */
    @PostMapping
    @PermitAll
    public ResponseEntity<AddressMasterDto> create(@Valid @RequestBody AddressMasterDto dto) {
        AddressMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/addresses/" + saved.getId())).body(saved);
    }

    /**
     * Update address
     */
    @PutMapping("/{id}")

    @PermitAll
    public AddressMasterDto update(@PathVariable Integer id, @Valid @RequestBody AddressMasterDto dto) {
        return service.update(id, dto);
    }

    /**
     * Delete address
     */
    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search addresses by company and keyword
     * Equivalent to legacy .NET SelectAddress method
     * GET /api/addresses/company/6/search?keyword=NewYork
     * GET /api/addresses/company/6/search  (without keyword - returns all active)
     *
     * @param companyRefId the company ID
     * @param keyword optional search keyword (address name contains)
     * @return list of active addresses matching criteria ordered by name
     */
    @GetMapping("/company/{companyRefId}/search")
    @PermitAll
    public ResponseEntity<Map<String, Object>> searchAddresses(
            @PathVariable Integer companyRefId,
            @RequestParam(required = false, defaultValue = "") String keyword) {

        List<AddressMasterDto> data = service.searchAddresses(companyRefId, keyword);

        Map<String, Object> response = Map.of(
                "ok", true,
                "message", "Search successful",
                "data", data,
                "count", data.size()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Get all active addresses for a company
     * Returns only addresses with active != 2
     * Ordered by name
     *
     * GET /api/addresses/company/6/active
     *
     * @param companyRefId the company ID
     * @return list of active addresses ordered by name
     */
    @GetMapping("/company/{companyRefId}/active")
    @PermitAll
    public ResponseEntity<Map<String, Object>> getActiveAddresses(@PathVariable Integer companyRefId) {
        List<AddressMasterDto> data = service.getActiveAddressesByCompany(companyRefId);

        Map<String, Object> response = Map.of(
                "ok", true,
                "message", "Active addresses retrieved successfully",
                "data", data,
                "count", data.size()
        );

        return ResponseEntity.ok(response);
    }
}
