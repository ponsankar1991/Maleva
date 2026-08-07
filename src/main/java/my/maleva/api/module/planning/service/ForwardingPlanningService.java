package my.maleva.api.module.planning.service;

import my.maleva.api.module.planning.dto.ForwardingPlanningReportDto;
import java.time.LocalDate;
import java.util.List;

public interface ForwardingPlanningService {
    List<ForwardingPlanningReportDto> getForwardingPlanningReport(Integer companyRefId, LocalDate fromDate, LocalDate toDate);
}
