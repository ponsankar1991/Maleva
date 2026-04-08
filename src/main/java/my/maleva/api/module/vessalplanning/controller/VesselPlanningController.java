package my.maleva.api.module.vessalplanning.controller;

import jakarta.validation.Valid;
import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.vessalplanning.dto.VesselPlanningDetailsDto;
import my.maleva.api.module.vessalplanning.dto.VesselPlanningLegacyDtos;
import my.maleva.api.module.vessalplanning.dto.VesselPlanningMasterDto;
import my.maleva.api.module.vessalplanning.service.IVesselPlanningMasterService;
import my.maleva.api.module.vessalplanning.service.IVesselPlanningSaveService;
import my.maleva.api.module.vessalplanning.service.VesselPlanningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vessel-plannings")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_100')")
public class VesselPlanningController {

    private static final Logger logger = LoggerFactory.getLogger(VesselPlanningController.class);

    private final VesselPlanningService crudService;
    private final IVesselPlanningMasterService vesselPlanningMasterService;
    private final IVesselPlanningSaveService vesselPlanningSaveService;

    public VesselPlanningController(
            VesselPlanningService crudService,
            IVesselPlanningMasterService vesselPlanningMasterService,
            IVesselPlanningSaveService vesselPlanningSaveService) {
        this.crudService = crudService;
        this.vesselPlanningMasterService = vesselPlanningMasterService;
        this.vesselPlanningSaveService = vesselPlanningSaveService;
    }

    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<VesselPlanningMasterDto>> getByCompanyRefId(@PathVariable Integer companyRefId) { return ResponseEntity.ok(crudService.getByCompanyRefId(companyRefId)); }

    @GetMapping("/company/{companyRefId}/active")
    public ResponseEntity<List<VesselPlanningMasterDto>> getActiveByCompanyRefId(@PathVariable Integer companyRefId) { return ResponseEntity.ok(crudService.getActiveByCompanyRefId(companyRefId)); }

    @GetMapping("/c-number/{cNumber}/company/{companyRefId}")
    public ResponseEntity<?> getByCNumber(@PathVariable Integer cNumber, @PathVariable Integer companyRefId) {
        Optional<VesselPlanningMasterDto> record = crudService.getByCNumber(cNumber, companyRefId);
        return record.isPresent() ? ResponseEntity.ok(record.get()) : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }
    @GetMapping("/employee/{employeeRefId}")
    public ResponseEntity<List<VesselPlanningMasterDto>> getByEmployeeRefId(@PathVariable Integer employeeRefId) { return ResponseEntity.ok(crudService.getByEmployeeRefId(employeeRefId)); }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Optional<VesselPlanningMasterDto> record = crudService.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) : ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody VesselPlanningMasterDto dto) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(crudService.create(dto)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage()); }
    }

    @PostMapping("/process")
    public ResponseEntity<?> processVesselPlanning(@Valid @RequestBody VesselPlanningProcessRequest request, @RequestParam Integer companyId) {
        try {
            VesselPlanningMasterDto result = crudService.processVesselPlanning(request.getVesselPlanning(), request.getDetails(), companyId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody VesselPlanningMasterDto dto) {
        try { return ResponseEntity.ok(crudService.update(id, dto)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found"); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<VesselPlanningLegacyDtos.SaveResponse> delete(@PathVariable Integer id, @RequestParam Integer companyId) { return ResponseEntity.ok(vesselPlanningSaveService.delete(id, companyId)); }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateVesselPlanning(@PathVariable Integer id) {
        try { return ResponseEntity.ok(crudService.activateVesselPlanning(id)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found"); }
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivateVesselPlanning(@PathVariable Integer id) {
        try { return ResponseEntity.ok(crudService.deactivateVesselPlanning(id)); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found"); }
    }

    @GetMapping("/company/{companyRefId}/count")
    public ResponseEntity<?> countByCompanyRefId(@PathVariable Integer companyRefId) { return ResponseEntity.ok("Total: " + crudService.countByCompanyRefId(companyRefId)); }

    @GetMapping("/company/{companyRefId}/active/count")
    public ResponseEntity<?> countActiveByCompanyRefId(@PathVariable Integer companyRefId) { return ResponseEntity.ok("Total: " + crudService.countActiveByCompanyRefId(companyRefId)); }

    @PostMapping("/max-vessel-planning-no/{companyId}")
    public ResponseEntity<VesselPlanningLegacyDtos.NumberResponse> getMaxVesselPlanningNo(@PathVariable Integer companyId) {
        String sequenceNumber = vesselPlanningSaveService.getMaxVesselPlanningNo(companyId);
        return ResponseEntity.ok(VesselPlanningLegacyDtos.NumberResponse.builder().sequenceNumber(sequenceNumber).companyId(companyId).success(true).build());
    }

    @PostMapping("/select-vessel-planning")
    public ResponseEntity<VesselPlanningLegacyDtos.F5View> selectVesselPlanning(@RequestBody @Valid VesselPlanningLegacyDtos.F5Request filter) { return ResponseEntity.ok(vesselPlanningMasterService.selectVesselPlanning(filter)); }

    @GetMapping("/edit")
    public ResponseEntity<VesselPlanningLegacyDtos.EditResponse> editVesselPlanning(@RequestParam(required = false) Integer id, @RequestParam(required = false) Integer vesselPlanningNo, @RequestParam Integer companyId) { return ResponseEntity.ok(vesselPlanningMasterService.editVesselPlanning(id, vesselPlanningNo, companyId)); }

    @PostMapping("/search")
    public ResponseEntity<List<VesselPlanningLegacyDtos.DetailsModel>> search(@RequestBody @Valid VesselPlanningLegacyDtos.SearchRequest filter) { return ResponseEntity.ok(vesselPlanningMasterService.vesselPlanningSearch(filter)); }

    @PostMapping("/save")
    public ResponseEntity<List<VesselPlanningLegacyDtos.SaveResponse>> save(@RequestBody @Valid List<VesselPlanningLegacyDtos.SaveRequest> requests, @RequestHeader(value = "Comid", required = false) Integer companyIdHeader) {
        Integer companyId = resolveCompanyId(requests, companyIdHeader);
        logger.info("Saving {} vessel planning record(s) for company {}", requests.size(), companyId);
        return ResponseEntity.ok(vesselPlanningSaveService.saveAll(requests, companyId));
    }

    @PostMapping("/view")
    public ResponseEntity<List<VesselPlanningLegacyDtos.ViewModel>> view(@RequestBody @Valid VesselPlanningLegacyDtos.ViewRequest request) { return ResponseEntity.ok(vesselPlanningMasterService.vesselPlanningView(request.getSoId(), request.getComid())); }

    private Integer resolveCompanyId(List<VesselPlanningLegacyDtos.SaveRequest> requests, Integer companyIdHeader) {
        if (companyIdHeader != null && companyIdHeader > 0) return companyIdHeader;
        if (requests != null && !requests.isEmpty() && requests.get(0).getCompanyRefId() != null && requests.get(0).getCompanyRefId() > 0) return requests.get(0).getCompanyRefId();
        throw new InvalidRequestException("Company ID is required for vessel planning save");
    }

    public static class VesselPlanningProcessRequest {
        private VesselPlanningMasterDto vesselPlanning;
        private List<VesselPlanningDetailsDto> details;
        public VesselPlanningMasterDto getVesselPlanning() { return vesselPlanning; }
        public void setVesselPlanning(VesselPlanningMasterDto vesselPlanning) { this.vesselPlanning = vesselPlanning; }
        public List<VesselPlanningDetailsDto> getDetails() { return details; }
        public void setDetails(List<VesselPlanningDetailsDto> details) { this.details = details; }
    }
}

