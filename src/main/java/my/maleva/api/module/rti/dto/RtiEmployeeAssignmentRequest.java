package my.maleva.api.module.rti.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RtiEmployeeAssignmentRequest(
        @NotNull(message = "fromDate is required") LocalDate fromDate,
        @NotNull(message = "toDate is required") LocalDate toDate,
        Integer companyId,
        Integer employeeId
) {}
