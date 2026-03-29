package my.maleva.api.module.planning.controller;

import my.maleva.api.module.planning.dto.PlanningDetailsDto;
import my.maleva.api.module.planning.service.PlanningDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/planning-details")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PlanningDetailsController {

    private final PlanningDetailsService planningDetailsService;

    public PlanningDetailsController(PlanningDetailsService planningDetailsService) {
        this.planningDetailsService = planningDetailsService;
    }

    /**
     * Get all planning details
     * GET /api/planning-details
     */
    @GetMapping
    public List<PlanningDetailsDto> list() {
        return planningDetailsService.listAll();
    }

    /**
     * Get planning details by ID
     * GET /api/planning-details/{id}
     */
    @GetMapping("/{id}")
    public PlanningDetailsDto get(@PathVariable Integer id) {
        return planningDetailsService.getById(id);
    }

    /**
     * Create new planning details
     * POST /api/planning-details
     */
    @PostMapping
    public ResponseEntity<PlanningDetailsDto> create(@Valid @RequestBody PlanningDetailsDto dto) {
        PlanningDetailsDto saved = planningDetailsService.create(dto);
        return ResponseEntity.created(URI.create("/api/planning-details/" + saved.getId())).body(saved);
    }

    /**
     * Update planning details
     * PUT /api/planning-details/{id}
     */
    @PutMapping("/{id}")
    public PlanningDetailsDto update(@PathVariable Integer id, @Valid @RequestBody PlanningDetailsDto dto) {
        return planningDetailsService.update(id, dto);
    }

    /**
     * Delete planning details
     * DELETE /api/planning-details/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        planningDetailsService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all details by planning master reference ID
     * GET /api/planning-details/by-master/{masterRefId}
     */
    @GetMapping("/by-master/{masterRefId}")
    public List<PlanningDetailsDto> getByPlanningMasterId(@PathVariable Integer masterRefId) {
        return planningDetailsService.getByPlanningMasterId(masterRefId);
    }

    /**
     * Get all details by sale order master reference ID
     * GET /api/planning-details/by-sale-order/{saleOrderMasterId}
     */
    @GetMapping("/by-sale-order/{saleOrderMasterId}")
    public List<PlanningDetailsDto> getBySaleOrderMasterId(@PathVariable Integer saleOrderMasterId) {
        return planningDetailsService.getBySaleOrderMasterId(saleOrderMasterId);
    }

    /**
     * Get all details by truck reference ID
     * GET /api/planning-details/by-truck/{truckRefId}
     */
    @GetMapping("/by-truck/{truckRefId}")
    public List<PlanningDetailsDto> getByTruckId(@PathVariable Integer truckRefId) {
        return planningDetailsService.getByTruckId(truckRefId);
    }

    /**
     * Delete all details by planning master reference ID
     * DELETE /api/planning-details/by-master/{masterRefId}
     */
    @DeleteMapping("/by-master/{masterRefId}")
    public ResponseEntity<Void> deleteByPlanningMasterId(@PathVariable Integer masterRefId) {
        planningDetailsService.deleteByPlanningMasterId(masterRefId);
        return ResponseEntity.noContent().build();
    }
}


