package my.maleva.api.module.planning.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * PlanningF5RequestDto - Request DTO for planning F5 view
 * Equivalent to .NET F5ViewModel
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningF5RequestDto {

    @NotNull(message = "Company ID is required")
    private Integer comid;

    private Integer employeeid;

    private String search;

    private LocalDate fromdate;

    private LocalDate todate;
}
