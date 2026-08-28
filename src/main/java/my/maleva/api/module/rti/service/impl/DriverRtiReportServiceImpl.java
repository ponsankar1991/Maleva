package my.maleva.api.module.rti.service.impl;

import lombok.RequiredArgsConstructor;
import my.maleva.api.common.dto.ApiResponse;
import my.maleva.api.module.rti.dto.DriverRtiReportDto;
import my.maleva.api.module.rti.dto.DriverRtiReportRequest;
import my.maleva.api.module.rti.repository.DriverRtiReportRepository;
import my.maleva.api.module.rti.service.DriverRtiReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deliberately NOT {@code @Transactional}: the one method here catches and
 * wraps its own errors, and inside a transaction a repository failure marks it
 * rollback-only — the caught error is then replaced at commit by an opaque
 * "Transaction silently rolled back" 500. The report is a single
 * JdbcTemplate read, which needs no transaction at all.
 */
@Service
@RequiredArgsConstructor
public class DriverRtiReportServiceImpl implements DriverRtiReportService {

    private static final Logger log = LoggerFactory.getLogger(DriverRtiReportServiceImpl.class);

    private final DriverRtiReportRepository repository;

    @Override
    public ApiResponse<List<DriverRtiReportDto>> getDriverRtiReport(DriverRtiReportRequest request) {
        try {
            List<DriverRtiReportDto> list = repository.findReport(request);
            if (list == null || list.isEmpty()) {
                return ApiResponse.error("No records found", HttpStatus.NOT_FOUND.value());
            }

            List<DriverRtiReportDto> sorted = list.stream()
                    .sorted(Comparator.comparing(DriverRtiReportDto::getDriverName, Comparator.nullsFirst(String::compareTo))
                            .thenComparing(DriverRtiReportDto::getCNumberDisplay, Comparator.nullsFirst(String::compareTo)))
                    .collect(Collectors.toList());

            return ApiResponse.success(sorted, "Success");
        } catch (Exception ex) {
            log.error("Error while preparing Driver RTI report", ex);
            return ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}


