package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * Request payload for creating a customer in QNE cloud accounting.
 * Legacy: CustomerQNEInsertModel (Models/Master/CustomerModel.cs).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneCustomerRequest {
    private String companyCode;
    private String companyName;
    private String companyName2;
    private String controlAccount;
    private String registrationNo;
    private String gstRegNo;
    private String category;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String contactPerson;
    private String email;
    private String phoneNo1;
    private String phoneNo2;
    private String faxNo1;
    private String faxNo2;
    private String businessNature;
    private String homepage;
    private String area;
    private String term;
    private String salesPerson;
    private String currency;
    private String defaultTaxCode;
    private String sourceOfLead;
    private String status;
}
