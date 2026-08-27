package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * Supplier insert request sent to the QNE cloud accounting API.
 * Legacy: SupplierQNEInsertModel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneSupplierRequest {

    private String companyCode;
    private String companyName;
    private String companyName2;
    private String controlAccount;
    private String registrationNo;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String contactPerson;
    private String phoneNo1;
    private String phoneNo2;
    private String faxNo1;
    private String faxNo2;
    private String email;
    private String homepage;
    private String businessNature;
    @JsonProperty("IsProspect")
    private boolean isProspect;
    @JsonProperty("IsSuspended")
    private boolean isSuspended;
    private String category;
    private String deliveryAddress1;
    private String deliveryAddress2;
    private String deliveryAddress3;
    private String deliveryAddress4;
    private String area;
    private String purchaser;
    private String currency;
    private String term;
    @JsonProperty("IsExceedCreditAllowed")
    private boolean isExceedCreditAllowed;
    @JsonProperty("IsTaxExempted")
    private boolean isTaxExempted;
    private String billingContactPerson;
    private String billingContactPhoneNo;
    private String billingContactEmail;
    private String accountContactPerson;
    private String accountContactPhoneNo;
    private String accountContactEmail;
    private String managementContactPerson;
    private String managementContactPhoneNo;
    private String managementContactEmail;
    private String guarantorName;
    private String guarantorIdNo;
    private String guarantorGender;
    private String guarantorRace;
    private String guarantorCitizenship;
    private String gstExemptionNo;
    private String gstExemptionExpiryDate;
    private String startDate;
    private String gstRegNo;
    private String defaultTaxCode;
    private String gstStatusVerifiedDate;
    private String defaultWTaxCode;
    private String branchCode;
}
