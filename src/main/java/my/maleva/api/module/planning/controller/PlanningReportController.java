package my.maleva.api.module.planning.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.planning.dto.ForwardingPlanningReportDto;
import my.maleva.api.module.planning.service.ForwardingPlanningService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/planning/reports")
@RequiredArgsConstructor
@Tag(name = "Planning Reports API", description = "Endpoints for generating planning reports")
public class PlanningReportController {

    private final ForwardingPlanningService forwardingPlanningService;

    @Operation(summary = "Get Forwarding Planning Report")
    @GetMapping("/forwarding")
    public ResponseEntity<ApiResponse<List<ForwardingPlanningReportDto>>> getForwardingPlanningReport(
            @RequestParam("companyRefId") Integer companyRefId,
            @RequestParam("fromDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        List<ForwardingPlanningReportDto> report = forwardingPlanningService.getForwardingPlanningReport(companyRefId, fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success(report, "Report generated successfully"));
    }
}
