package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.fleet.dto.MaintenanceDashboardDto;
import my.maleva.api.module.fleet.dto.MaintenanceSpendDto;
import my.maleva.api.module.fleet.service.FleetExpiryService;
import my.maleva.api.module.fleet.service.MaintenanceSpendService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Fleet paperwork and servicing that has expired or is about to. */
@RestController
@RequestMapping("/api/fleet")
@Validated
@PermitAll
public class FleetExpiryController {

    private final FleetExpiryService service;
    private final MaintenanceSpendService spendService;

    public FleetExpiryController(FleetExpiryService service, MaintenanceSpendService spendService) {
        this.service = service;
        this.spendService = spendService;
    }

    /**
     * The maintenance dashboard: counts, a category breakdown, and every alert
     * ranked by urgency.
     *
     * @param horizonDays  how far ahead to look, default 10
     * @param criticalDays inside this many days an alert turns critical, default 5
     */
    @GetMapping("/maintenance-dashboard")
    public ResponseEntity<ApiResponse<MaintenanceDashboardDto>> getDashboard(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) Integer horizonDays,
            @RequestParam(required = false) Integer criticalDays) {

        MaintenanceDashboardDto data = service.getDashboard(companyRefId, horizonDays, criticalDays);
        return ResponseEntity.ok(ApiResponse.success(data, "Maintenance dashboard retrieved"));
    }

    /**
     * Maintenance spending between two dates (inclusive): job orders truck-wise
     * and job-type-wise, bill orders description-wise, and AutoPass / Toll /
     * Levi entry costs. Defaults to the last 30 days when no range is given.
     */
    @GetMapping("/maintenance-spend")
    public ResponseEntity<ApiResponse<MaintenanceSpendDto>> getSpend(
            @RequestParam Integer companyRefId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        LocalDate effectiveTo = toDate != null ? toDate : LocalDate.now();
        LocalDate effectiveFrom = fromDate != null ? fromDate : effectiveTo.minusDays(30);
        if (effectiveFrom.isAfter(effectiveTo)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("fromDate must not be after toDate", 400));
        }

        MaintenanceSpendDto data = spendService.getSpend(companyRefId, effectiveFrom, effectiveTo);
        return ResponseEntity.ok(ApiResponse.success(data, "Maintenance spend retrieved"));
    }
}
