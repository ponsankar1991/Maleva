package my.maleva.api.controller;

import my.maleva.api.dto.PlanningMasterDto;
import my.maleva.api.service.PlanningMasterService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/planning-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PlanningMasterController {

    private final PlanningMasterService planningMasterService;

    public PlanningMasterController(PlanningMasterService planningMasterService) {
        this.planningMasterService = planningMasterService;
    }

    /**
     * Get all planning records
     * GET /api/planning-masters
     */
    @GetMapping
    public List<PlanningMasterDto> list() {
        return planningMasterService.listAll();
    }

    /**
     * Get planning record by ID
     * GET /api/planning-masters/{id}
     */
    @GetMapping("/{id}")
    public PlanningMasterDto get(@PathVariable Integer id) {
        return planningMasterService.getById(id);
    }

    /**
     * Create new planning record with details
     * POST /api/planning-masters
     */
    @PostMapping
    public ResponseEntity<PlanningMasterDto> create(@Valid @RequestBody PlanningMasterDto dto) {
        PlanningMasterDto saved = planningMasterService.create(dto);
        return ResponseEntity.created(URI.create("/api/planning-masters/" + saved.getId())).body(saved);
    }

    /**
     * Update planning record
     * PUT /api/planning-masters/{id}
     */
    @PutMapping("/{id}")
    public PlanningMasterDto update(@PathVariable Integer id, @Valid @RequestBody PlanningMasterDto dto) {
        return planningMasterService.update(id, dto);
    }

    /**
     * Delete planning record
     * DELETE /api/planning-masters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        planningMasterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get planning records by company and date range
     * GET /api/planning-masters/search/date-range?companyId=1&fromDate=2026-02-15T00:00:00&toDate=2026-03-15T23:59:59
     */
    @GetMapping("/search/date-range")
    public List<PlanningMasterDto> getByDateRange(
            @NotNull @RequestParam Integer companyId,
            @NotNull @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @NotNull @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return planningMasterService.getByCompanyAndDateRange(companyId, fromDate, toDate);
    }

    /**
     * Search planning records by keyword
     * GET /api/planning-masters/search?companyId=1&keyword=test
     */
    @GetMapping("/search")
    public List<PlanningMasterDto> search(
            @NotNull @RequestParam Integer companyId,
            @NotNull @RequestParam String keyword) {
        return planningMasterService.search(companyId, keyword);
    }

    /**
     * Get planning records by company and employee
     * GET /api/planning-masters/employee/{employeeId}?companyId=1
     */
    @GetMapping("/employee/{employeeId}")
    public List<PlanningMasterDto> getByCompanyAndEmployee(
            @NotNull @RequestParam Integer companyId,
            @PathVariable Integer employeeId) {
        return planningMasterService.getByCompanyAndEmployee(companyId, employeeId);
    }
}

