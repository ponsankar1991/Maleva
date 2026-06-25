package my.maleva.api.module.rti.controller;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.rti.dto.DriverRtiReportDto;
import my.maleva.api.module.rti.dto.DriverRtiReportRequest;
import my.maleva.api.module.rti.service.DriverRtiReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// ...existing code... (validation handled at controller advice level if present)
import java.util.List;

@RestController
@RequestMapping("/api/rti")
@RequiredArgsConstructor
public class DriverRtiReportController {

    private final DriverRtiReportService service;

    @PostMapping("/driver-report")
    public ResponseEntity<ApiResponse<List<DriverRtiReportDto>>> driverRTIReport(@RequestBody DriverRtiReportRequest request) {
        ApiResponse<List<DriverRtiReportDto>> response = service.getDriverRtiReport(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.valueOf(response.getStatusCode())).body(response);
    }
}


