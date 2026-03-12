package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SaleF5View - Response DTO combining master and detail sale orders
 * Equivalent to the .NET SaleF5view ViewModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleF5View {

    @JsonProperty("salemaster")
    private List<SaleMasterViewModel> salemaster;

    @JsonProperty("saledetails")
    private List<SaleDetailsViewModel> saledetails;
}

