package my.maleva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    private Integer id;
    private Integer companyRefId;
    private String customerName;
    private String cNumberDisplay;
    private Integer cNumber;
    private String address1;
    private String address2;
    private String city;
    private String zipcode;
    private String country;
    private Integer symbolRefid;
    private Integer paymentTermsRefid;
    private String gstNo;
    private String email;
    private String mobileNo;
    private String userName;
    private String password;
    private String latitude;
    private String longitude;
    private String tokenId;
    private String oEmail;
    private String oName;
    private String oPhone;
    private String aEmail;
    private String aName;
    private String aPhone;
    private Integer active;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    private String aEmail1;
    private String oEmail1;
    private String state;
    private String address3;
    private String personId;
    private BigDecimal openingBalance;
    private Integer accountRefid;
    private String tinNo;
    private String sstNo;
    private String msicCode;
    private String serviceTaxType;
    private String bankName;
    private String accountNo;
    private String companyCode;
    private String updateId;
    private String tintype;
    private String customerTin;
    private String eInvoice;
    private String exemptionNo;
    private LocalDate expiryDate;
    private String exemptionDetails;
    private String registrationNo;
    private String customerCity;
    private Integer countryId;
}
