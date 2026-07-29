package my.maleva.api.module.boardingsettlement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.boardingsettlement.dto.BoardingSalaryReportDto;
import my.maleva.api.module.boardingsettlement.service.BoardingSalaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import my.maleva.api.module.boardingsettlement.service.BoardingEventSyncService;

@RestController
@RequestMapping("/api/boarding-settlement")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Boarding Settlement", description = "Endpoints for Boarding Officer Salary and Settlement calculations")
public class BoardingSettlementController {

    private final BoardingSalaryService boardingSalaryService;
    private final BoardingEventSyncService boardingEventSyncService;

    @Operation(summary = "Calculate Monthly Boarding Salary", description = "Calculates officer salaries between fromDate and toDate. If 10 jobs exist on the same vessel/day, they are combined into one row. Rate is based on distinct officers on the vessel.")
    @GetMapping("/monthly-salary")
    public ResponseEntity<ApiResponse<List<BoardingSalaryReportDto>>> getMonthlySalaryReport(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate,
            @RequestParam(value = "employeeId", required = false) Integer employeeId) {

        log.info("REST request to get Monthly Salary Report - fromDate: {}, toDate: {}, employeeId: {}", fromDate, toDate, employeeId);

        List<BoardingSalaryReportDto> report = boardingSalaryService.calculateMonthlySalary(fromDate, toDate, employeeId);

        return ResponseEntity.ok(ApiResponse.success(report, "Successfully calculated boarding salary report"));
    }

    @Operation(summary = "Sync All Historical Jobs", description = "One-off utility to loop through all existing SaleOrderMaster jobs and backfill them into the BoardingEvent table.")
    @PostMapping("/sync-all-historical-jobs")
    public ResponseEntity<ApiResponse<String>> syncAllHistoricalJobs() {
        log.info("REST request to sync all historical jobs to BoardingEvent");
        int count = boardingEventSyncService.syncAllHistoricalJobs();
        return ResponseEntity.ok(ApiResponse.success("Successfully synced " + count + " jobs to BoardingEvent table.", "Sync Complete"));
    }
}

