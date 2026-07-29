package my.maleva.api.module.boardingsettlement.service;

import my.maleva.api.module.boardingsettlement.dto.BoardingSalaryReportDto;
import java.util.List;

public interface BoardingSalaryService {
    List<BoardingSalaryReportDto> calculateMonthlySalary(String fromDate, String toDate, Integer employeeId);
}
