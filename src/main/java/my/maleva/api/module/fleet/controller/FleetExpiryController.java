package my.maleva.api.module.fleet.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.fleet.dto.MaintenanceDashboardDto;
import my.maleva.api.module.fleet.service.FleetExpiryService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Fleet paperwork and servicing that has expired or is about to. */
@RestController
@RequestMapping("/api/fleet")
@Validated
@PermitAll
public class FleetExpiryController {

    private final FleetExpiryService service;

    public FleetExpiryController(FleetExpiryService service) {
        this.service = service;
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
}
