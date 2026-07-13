package my.maleva.api.module.rti.repository;

import my.maleva.api.module.rti.dto.RtiJobWiseViewResponse;

import java.time.LocalDate;
import java.util.List;

public interface RtiJobWiseRepository {
    List<RtiJobWiseViewResponse> findJobWiseView(LocalDate fromDate, LocalDate toDate);
}
