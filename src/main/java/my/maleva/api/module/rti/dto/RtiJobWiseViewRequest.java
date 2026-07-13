package my.maleva.api.module.rti.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RtiJobWiseViewRequest(
        @NotNull(message = "fromDate is required")
        LocalDate fromDate,

        @NotNull(message = "toDate is required")
        LocalDate toDate
) {}
