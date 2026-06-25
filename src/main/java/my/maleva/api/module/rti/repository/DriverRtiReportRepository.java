package my.maleva.api.module.rti.repository;

import my.maleva.api.module.rti.dto.DriverRtiReportDto;
import my.maleva.api.module.rti.dto.DriverRtiReportRequest;

import java.util.List;

public interface DriverRtiReportRepository {
    List<DriverRtiReportDto> findReport(DriverRtiReportRequest request);
}

