package my.maleva.api.module.customer.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSelectDto {

    // =========================
    // Core Identifiers
    // =========================
    private Integer id;
    private Integer companyRefId;
    private Integer customerMasterRefId;

    // =========================
    // Customer Number & Names
    // =========================
    private String cNumberDisplay;
    private Integer cNumber;
    private String customerName;
    private String name;

    // =========================
    // Contact Information
    // =========================
    private String mobileNo;
    private String whatsapp;
    private String email;
    private String userName;
    private String password;
    private String personId;

    // =========================
    // Address Details
    // =========================
    private String address1;
    private String address2;
    private String address3;
    private String city;
    private String state;
    private String zipcode;
    private Integer countryId;
    private String country;
    private String customerCity;

    // =========================
    // Master / Joined Display Fields
    // =========================
    private String cmName;
    private String sName;
    private String termsName;

    // =========================
    // Currency & Payment
    // =========================
    private Integer symbolRefId;
    private Integer paymentTermsRefId;

    // =========================
    // Tax & Registration Info
    // =========================
    private String gstNo;
    private String tinNo;
    private String sstNo;
    private String tinType;
    private String customerTin;
    private String registrationNo;
    private String msicCode;
    private String serviceTaxType;
    private String eInvoice;
    private String exemptionNo;
    private String exemptionDetails;

    // =========================
    // Bank & Accounting
    // =========================
    private String bankName;
    private String accountNo;
    private String accountCode;
    private String companyCode;

    // =========================
    // Location Info
    // =========================
    private String latitude;
    private String longitude;

    // =========================
    // Owner Contact Info
    // =========================
    private String oEmail;
    private String oEmail1;
    private String oName;
    private String oPhone;

    // =========================
    // Alternate Contact Info
    // =========================
    private String aEmail;
    private String aEmail1;
    private String aName;
    private String aPhone;

    // =========================
    // Auth / Token (as requested)
    // =========================
    private String tokenId;

    // =========================
    // Status & Audit
    // =========================
    private Integer active;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;

    // =========================
    // Misc
    // =========================
    private String updateId;
    private String expiryDate;
}
