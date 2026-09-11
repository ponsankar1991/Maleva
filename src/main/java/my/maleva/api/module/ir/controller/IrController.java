package my.maleva.api.module.ir.controller;

import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.ir.dto.IrDepartmentOptionDto;
import my.maleva.api.module.ir.dto.IrDetailDto;
import my.maleva.api.module.ir.dto.IrListResponse;
import my.maleva.api.module.ir.dto.IrSaveRequest;
import my.maleva.api.module.ir.dto.IrSearchRequest;
import my.maleva.api.module.ir.dto.IrStatusOptionDto;
import my.maleva.api.module.ir.service.IrService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Incident reports.
 *
 * <p>Query parameters are camelCase because that is what the servlet binds -
 * a PascalCase name binds to nothing, and the filter then silently returns
 * every row instead of failing.
 *
 * <p>No {@code @PreAuthorize} here on purpose: role gates that the screen's
 * users do not all satisfy fail as an empty grid or an empty dropdown rather
 * than as a visible error, and the company scoping in the service is what
 * actually keeps one company's rows away from another.
 *
 * <p>Attachments are not served here. The screen files them through
 * {@code /api/attachments} with {@code folderName=IRReport} and
 * {@code filePathTable=IRMaster}, which keeps {@code IRMaster.FilePath} in sync.
 */
@RestController
@RequestMapping("/api/ir")
public class IrController {

    private final IrService irService;

    public IrController(IrService irService) {
        this.irService = irService;
    }

    /** The filtered list with its total. */
    @GetMapping
    public ResponseEntity<ApiResponse<IrListResponse>> search(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer irStatusRefId,
            @RequestParam(required = false) Integer departmentRefId,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String vesselName,
            @RequestParam(required = false) Integer truckRefId,
            @RequestParam(required = false) String truckNo,
            @RequestParam(required = false) Integer employeeRefId,
            @RequestParam(required = false) Integer driverRefId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean openOnly) {

        IrListResponse data = irService.search(IrSearchRequest.builder()
                .companyRefId(companyRefId)
                .fromDate(fromDate)
                .toDate(toDate)
                .irStatusRefId(irStatusRefId)
                .departmentRefId(departmentRefId)
                .departmentName(departmentName)
                .vesselName(vesselName)
                .truckRefId(truckRefId)
                .truckNo(truckNo)
                .employeeRefId(employeeRefId)
                .driverRefId(driverRefId)
                .search(search)
                .openOnly(openOnly)
                .build());

        return ResponseEntity.ok(ApiResponse.success(data, "IR list retrieved"));
    }

    /** One IR for the edit form. */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IrDetailDto>> getById(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId) {

        return ResponseEntity.ok(
                ApiResponse.success(irService.getById(id, companyRefId), "IR retrieved"));
    }

    /** Creates when the body has no id, updates that row otherwise. */
    @PostMapping
    public ResponseEntity<ApiResponse<IrDetailDto>> save(
            @Valid @RequestBody IrSaveRequest request,
            Authentication authentication) {

        IrDetailDto saved = irService.save(request, usernameOf(authentication));
        return ResponseEntity.ok(ApiResponse.success(saved, "IR saved"));
    }

    /** Soft delete: the row stays with Active = 2. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id,
            @RequestParam Integer companyRefId,
            Authentication authentication) {

        irService.delete(id, companyRefId, usernameOf(authentication));
        return ResponseEntity.ok(ApiResponse.success(null, "IR deleted"));
    }

    /** Status dropdown. */
    @GetMapping("/statuses")
    public ResponseEntity<ApiResponse<List<IrStatusOptionDto>>> statuses(
            @RequestParam Integer companyRefId) {

        return ResponseEntity.ok(
                ApiResponse.success(irService.statuses(companyRefId), "IR statuses retrieved"));
    }

    /** Department dropdown, from the UserRoles enum. */
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<IrDepartmentOptionDto>>> departments() {
        return ResponseEntity.ok(
                ApiResponse.success(irService.departments(), "Departments retrieved"));
    }

    private String usernameOf(Authentication authentication) {
        return authentication == null ? "system" : authentication.getName();
    }
}
