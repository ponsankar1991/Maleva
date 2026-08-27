package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * Response payload for a customer read from QNE cloud accounting (pull/backfill sync).
 * Only the fields the sync reads are declared; unknown fields are ignored.
 * Legacy: CustomerQNEModel (Models/Master/CustomerModel.cs).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class QneCustomerResponse {
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
    private String status;
}
