package my.maleva.api.module.rti.controller;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.rti.dto.RtiEmployeeAssignmentRequest;
import my.maleva.api.module.rti.dto.RtiEmployeeAssignmentResponse;
import my.maleva.api.module.rti.dto.RtiJobWiseViewRequest;
import my.maleva.api.module.rti.dto.RtiJobWiseViewResponse;
import my.maleva.api.module.rti.service.RtiJobWiseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rti")
public class RtiJobWiseController {

    private static final Logger logger = LoggerFactory.getLogger(RtiJobWiseController.class);
    private final RtiJobWiseService service;

    public RtiJobWiseController(RtiJobWiseService service) {
        this.service = service;
    }

    @PermitAll
    @PostMapping("/job-wise-view")
    public ResponseEntity<ApiResponse<List<RtiJobWiseViewResponse>>> getJobWiseView(
            @Valid @RequestBody RtiJobWiseViewRequest request) {
        
        logger.debug("Received request for RTI Job Wise View: fromDate={}, toDate={}", 
                request.fromDate(), request.toDate());

        List<RtiJobWiseViewResponse> response = service.getJobWiseView(request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Job wise view fetched successfully"));
    }

    @PermitAll
    @PostMapping("/employee-assignments")
    public ResponseEntity<ApiResponse<List<RtiEmployeeAssignmentResponse>>> getEmployeeAssignments(
            @Valid @RequestBody RtiEmployeeAssignmentRequest request) {
        
        logger.debug("Received request for RTI Employee Assignments: fromDate={}, toDate={}, companyId={}, employeeId={}", 
                request.fromDate(), request.toDate(), request.companyId(), request.employeeId());

        List<RtiEmployeeAssignmentResponse> response = service.getEmployeeAssignments(request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Employee assignments fetched successfully"));
    }
}
