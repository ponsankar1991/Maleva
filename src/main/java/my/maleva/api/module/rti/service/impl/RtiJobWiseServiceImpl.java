package my.maleva.api.module.rti.service.impl;

import my.maleva.api.common.exception.DateRangeTooLargeException;
import my.maleva.api.common.exception.InvalidDateRangeException;
import my.maleva.api.module.rti.dto.RtiJobWiseViewRequest;
import my.maleva.api.module.rti.dto.RtiJobWiseViewResponse;
import my.maleva.api.module.rti.repository.RtiJobWiseRepository;
import my.maleva.api.module.rti.service.RtiJobWiseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RtiJobWiseServiceImpl implements RtiJobWiseService {

    private final RtiJobWiseRepository repository;
    private final int maxDateRangeDays;

    public RtiJobWiseServiceImpl(
            RtiJobWiseRepository repository,
            @Value("${rti.job-wise.max-date-range-days:90}") int maxDateRangeDays) {
        this.repository = repository;
        this.maxDateRangeDays = maxDateRangeDays;
    }

    @Override
    public List<RtiJobWiseViewResponse> getJobWiseView(RtiJobWiseViewRequest request) {
        LocalDate from = request.fromDate();
        LocalDate to = request.toDate();

        if (from.isAfter(to)) {
            throw new InvalidDateRangeException("fromDate cannot be after toDate.");
        }

        long daysBetween = ChronoUnit.DAYS.between(from, to);
        if (daysBetween > maxDateRangeDays) {
            throw new DateRangeTooLargeException(
                    String.format("Date range exceeds the maximum allowed limit of %d days.", maxDateRangeDays));
        }

        return repository.findJobWiseView(from, to);
    }
}
