package my.maleva.api.module.rti.service;

import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.rti.dto.DriverRtiReportDto;
import my.maleva.api.module.rti.dto.DriverRtiReportRequest;

import java.util.List;

public interface DriverRtiReportService {
    ApiResponse<List<DriverRtiReportDto>> getDriverRtiReport(DriverRtiReportRequest request);
}

