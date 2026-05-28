package my.maleva.api.module.supplier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SupplierExtendedResponse - DTO for extended supplier data with joined master data
 * Equivalent to .NET SelectSupplierAll model
 *
 * This DTO is used when fetching suppliers with joined SymbolMaster,
 * PaymentTermsMaster, and AccountsGroupMaster data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierExtendedResponse {

    // Supplier basic fields
    private Integer id;
    private Integer companyRefId;
    private String cNumberDisplay;
    private Integer cNumber;
    private String supplierName;
    private String address1;
    private String address2;
    private String address3;
    private String city;
    private String state;
    private String zipcode;
    private String country;
    private String supplierCity;

    // Reference IDs
    private Integer symbolRefid;
    private Integer paymentTermsRefid;
    private Integer countryId;
    private Integer accountRefid;
    private Integer msicCodeRefId;

    // Identification
    private String personId;
    private String supplierType;

    // Contact information
    private String email;
    private String oEmail;
    private String oEmail1;
    private String mobileNo;
    private String oPhone;
    private String aPhone;
    private String userName;

    // Tax and compliance
    private String gstNo;
    private String tinNo;
    private String sstNo;
    private String msicCode;
    private String serviceTaxType;
    private String tinType;
    private String supplierTin;
    private String taxExemptionNo;
    private String taxExemptionDetails;
    private String registrationNo;

    // Banking details
    private String bankName;
    private String accountNo;

    // System fields
    private String tokenId;
    private String oName;
    private String aName;
    private String password;
    private String latitude;
    private String longitude;
    private Integer active;
    private String createdDate;
    private String modifiedDate;
    private String modifiedBy;
    private Integer selfBilled;

    // ===== JOINED DATA FROM MASTER TABLES =====

    /**
     * SName - Symbol Name from SymbolMaster.SName
     */
    private String sName;

    /**
     * TermsName - Payment Terms Name from PaymentTermsMaster.TermsName
     */
    private String termsName;

    /**
     * AccountCode - Account Code from AccountsGroupMaster.AccountCode
     */
    private String accountCode;

    // Additional audit fields
    private String qneCode;
    private String qneId;
}

