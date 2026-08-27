package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * Supplier response read from the QNE cloud accounting API (only the fields the sync reads).
 * Legacy: SupplierQNEModel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QneSupplierResponse {

    private String id;
    private String companyCode;
    private String companyName;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String contactPerson;
    private String email;
    private String phoneNo1;
    private String term;
    private String currency;
}
