package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * PlanningF5View - Response DTO combining master and detail planning data
 * Equivalent to the .NET PLANINGF5view ViewModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningF5View {

    @JsonProperty("salemaster")
    private List<PlanningMasterViewModel> salemaster;

    @JsonProperty("saledetails")
    private List<PlanningDetailsModel> saledetails;
}
