package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettingsDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @Size(max = 200)
    private String companyname;

    @Size(max = 200)
    private String address1;

    @Size(max = 200)
    private String address2;

    @Size(max = 200)
    private String city;

    @Size(max = 200)
    private String pincode;

    @Size(max = 200)
    private String phone;

    @Size(max = 100)
    private String gstNo;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 100)
    private String state;

    @Size(max = 500)
    private String footerMsg1;

    @Size(max = 500)
    private String footerMsg2;

    @NotNull
    private Integer noOfBills;

    @NotBlank
    @Size(max = 100)
    private String billType;

    @NotBlank
    @Size(max = 100)
    private String billColumn;

    private Integer posQty;

    @NotBlank
    @Size(max = 200)
    private String posTax;

    @NotNull
    private Boolean negativeStock;

    @NotNull
    private Boolean multiMrp;

    @Size(max = 50)
    private String pcodePrefix;

    private Integer pcodeDigits;

    @NotNull
    private Boolean pcodeAuto;

    @NotBlank
    @Size(max = 100)
    private String roundOff;

    @Size(max = 200)
    private String posLine1;

    @Size(max = 200)
    private String posLine2;

    @Size(max = 200)
    private String posLine3;

    @Size(max = 200)
    private String posLine4;

    @Size(max = 200)
    private String posLine5;

    @Size(max = 200)
    private String srLine1;

    @Size(max = 200)
    private String srLine2;

    @Size(max = 200)
    private String srLine3;

    @Size(max = 200)
    private String srLine4;

    @Size(max = 200)
    private String srLine5;

    @Size(max = 200)
    private String poLine1;

    @Size(max = 200)
    private String poLine2;

    @Size(max = 200)
    private String poLine3;

    @Size(max = 200)
    private String poLine4;

    @Size(max = 200)
    private String poLine5;

    @Size(max = 200)
    private String soLine1;

    @Size(max = 200)
    private String soLine2;

    @Size(max = 200)
    private String soLine3;

    @Size(max = 200)
    private String soLine4;

    @Size(max = 200)
    private String soLine5;

    @Size(max = 200)
    private String prLine1;

    @Size(max = 200)
    private String prLine2;

    @Size(max = 200)
    private String prLine3;

    @Size(max = 200)
    private String prLine4;

    @Size(max = 200)
    private String prLine5;

    @Size(max = 200)
    private String qoLine1;

    @Size(max = 200)
    private String qoLine2;

    @Size(max = 200)
    private String qoLine3;

    @Size(max = 200)
    private String qoLine4;

    @Size(max = 200)
    private String qoLine5;

    @Size(max = 200)
    private String dcLine1;

    @Size(max = 200)
    private String dcLine2;

    @Size(max = 200)
    private String dcLine3;

    @Size(max = 200)
    private String dcLine4;

    @Size(max = 200)
    private String dcLine5;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 100)
    private String modifiedBy;

    @NotNull
    private Integer numberDigit;

    @Size(max = 50)
    private String billPrefix;

    private Long billNoStart;

    @Size(max = 100)
    private String topslogan;

    @NotNull
    private Float crmPointValue;

    @Size(max = 200)
    private String bankLine1;

    @Size(max = 200)
    private String bankLine2;

    @Size(max = 200)
    private String bankLine3;

    @Size(max = 200)
    private String bankLine4;

    @Size(max = 200)
    private String bankLine5;

    @Size(max = 100)
    private String saleBillFormat;

    @Size(max = 100)
    private String saleReturnBillFormat;

    @Size(max = 100)
    private String purchaseBillFormat;

    @Size(max = 100)
    private String purchaseReturnBillFormat;

    @Size(max = 100)
    private String purchaseOrderBillFormat;

    @Size(max = 100)
    private String saleOrderBillFormat;

    @Size(max = 100)
    private String quotationBillFormat;

    @Size(max = 100)
    private String dcBillFormat;

    @Size(max = 100)
    private String estimateBillFormat;

    @Size(max = 100)
    private String stockTransferBillFormat;

    @Size(max = 200)
    private String estimateFullName;

    @Size(max = 200)
    private String estimateCompanyName;

    @Size(max = 200)
    private String estimateAddress1;

    @Size(max = 200)
    private String estimateAddress2;

    @Size(max = 200)
    private String estimateCity;

    @Size(max = 200)
    private String estimatePhoneNo;

    @Size(max = 50)
    private String quotoPerfix;

    private Integer quotoDigit;

    @Size(max = 50)
    private String dcPerfix;

    private Integer dcDigit;

    @Size(max = 50)
    private String poPerfix;

    private Integer poDigit;

    @Size(max = 50)
    private String soPerfix;

    private Integer soDigit;

    @Size(max = 2000)
    private String accessToken;

    @Size(max = 100)
    private String expiresIn;

    @Size(max = 100)
    private String tokenType;

    @Size(max = 100)
    private String scope;

    @Size(max = 100)
    private String expiryDateTimeUtc;
}
