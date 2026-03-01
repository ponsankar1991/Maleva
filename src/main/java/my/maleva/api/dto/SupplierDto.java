package my.maleva.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * SupplierDto - DTO for Supplier
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotBlank(message = "Supplier Name is required")
    @Size(max = 500, message = "Supplier Name must not exceed 500 characters")
    private String supplierName;

    @NotBlank(message = "C Number Display is required")
    @Size(max = 300, message = "C Number Display must not exceed 300 characters")
    private String cNumberDisplay;

    @NotNull(message = "C Number is required")
    private Integer cNumber;

    @Size(max = 300, message = "Address1 must not exceed 300 characters")
    private String address1;

    @Size(max = 300, message = "Address2 must not exceed 300 characters")
    private String address2;

    @Size(max = 300, message = "Address3 must not exceed 300 characters")
    private String address3;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "Supplier City must not exceed 100 characters")
    private String supplierCity;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 50, message = "Zipcode must not exceed 50 characters")
    private String zipcode;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    @NotNull(message = "Symbol Reference ID is required")
    private Integer symbolRefid;

    @NotNull(message = "Payment Terms Reference ID is required")
    private Integer paymentTermsRefid;

    @Size(max = 100, message = "GST No must not exceed 100 characters")
    private String gstNo;

    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Email(message = "OEmail should be valid")
    @Size(max = 100, message = "OEmail must not exceed 100 characters")
    private String oEmail;

    @Email(message = "OEmail1 should be valid")
    @Size(max = 100, message = "OEmail1 must not exceed 100 characters")
    private String oEmail1;

    @Email(message = "AEmail should be valid")
    @Size(max = 100, message = "AEmail must not exceed 100 characters")
    private String aEmail;

    @Email(message = "AEmail1 should be valid")
    @Size(max = 100, message = "AEmail1 must not exceed 100 characters")
    private String aEmail1;

    @Size(max = 50, message = "Mobile No must not exceed 50 characters")
    private String mobileNo;

    @Size(max = 50, message = "OPhone must not exceed 50 characters")
    private String oPhone;

    @Size(max = 50, message = "APhone must not exceed 50 characters")
    private String aPhone;

    @Size(max = 50, message = "UserName must not exceed 50 characters")
    private String userName;

    @Size(max = 50, message = "Password must not exceed 50 characters")
    private String password;

    @Size(max = 50, message = "Latitude must not exceed 50 characters")
    private String latitude;

    @Size(max = 50, message = "Longitude must not exceed 50 characters")
    private String longitude;

    @Size(max = 500, message = "Token ID must not exceed 500 characters")
    private String tokenId;

    @Size(max = 500, message = "OName must not exceed 500 characters")
    private String oName;

    @Size(max = 500, message = "AName must not exceed 500 characters")
    private String aName;

    @Size(max = 100, message = "Person ID must not exceed 100 characters")
    private String personId;

    @NotNull(message = "Active status is required")
    private Integer active;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private String modifiedBy;

    private BigDecimal openingBalance;

    @NotBlank(message = "Supplier Type is required")
    @Size(max = 100, message = "Supplier Type must not exceed 100 characters")
    private String supplierType;

    @NotNull(message = "Account Reference ID is required")
    private Integer accountRefid;

    @Size(max = 100, message = "TIN No must not exceed 100 characters")
    private String tinNo;

    @Size(max = 100, message = "SST No must not exceed 100 characters")
    private String sstNo;

    @Size(max = 100, message = "MSIC Code must not exceed 100 characters")
    private String msicCode;

    @Size(max = 100, message = "Service Tax Type must not exceed 100 characters")
    private String serviceTaxType;

    @Size(max = 100, message = "Bank Name must not exceed 100 characters")
    private String bankName;

    @Size(max = 100, message = "Account No must not exceed 100 characters")
    private String accountNo;

    @Size(max = 50, message = "QNE Code must not exceed 50 characters")
    private String qneCode;

    @Size(max = 50, message = "QNE ID must not exceed 50 characters")
    private String qneId;

    private Integer selfBilled;

    @Size(max = 250, message = "TIN Type must not exceed 250 characters")
    private String tinType;

    @Size(max = 250, message = "Supplier TIN must not exceed 250 characters")
    private String supplierTin;

    private Integer msicCodeRefId;

    @Size(max = 250, message = "Tax Exemption No must not exceed 250 characters")
    private String taxExemptionNo;

    private LocalDate expiryDate;

    @Size(max = 500, message = "Tax Exemption Details must not exceed 500 characters")
    private String taxExemptionDetails;

    @Size(max = 250, message = "Registration No must not exceed 250 characters")
    private String registrationNo;
}

