package my.maleva.api.module.saleorder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JobNumberDto - Response DTO for GetCustJobNo endpoint
 * Maps to SaleOrderMaster projection: Id, CNumberDisplay
 *
 * Used to return customer job numbers for a given company and customer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobNumberDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("billNoDisplay")
    private String billNoDisplay;
}

