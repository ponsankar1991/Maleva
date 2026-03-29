package my.maleva.api.module.planning.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PLANINGSearchRequestDto - Request DTO for planning search
 * Equivalent to .NET PLANINGSearchModel
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PLANINGSearchRequestDto {

    @NotNull(message = "Company ID is required")
    private Integer comid;

    private String search;  // Comma-separated port codes (searches Sport and Oport)

    private String employeeid;  // empty, 0 or null for all employees

    @NotBlank(message = "From date is required")
    private String fromdate;  // Format: YYYY-MM-DD

    @NotBlank(message = "To date is required")
    private String todate;    // Format: YYYY-MM-DD
}

