package my.maleva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity for CompanySettings table
 */
@Entity
@Table(name = "CompanySettings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "Companyname", length = 200)
    private String companyname;

    @Column(name = "Address1", length = 200)
    private String address1;

    @Column(name = "Address2", length = 200)
    private String address2;

    @Column(name = "City", length = 200)
    private String city;

    @Column(name = "Pincode", length = 200)
    private String pincode;

    @Column(name = "Phone", length = 200)
    private String phone;

    @Column(name = "GSTNo", length = 100)
    private String gstNo;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "State", length = 100)
    private String state;

    @Column(name = "FooterMsg1", length = 500)
    private String footerMsg1;

    @Column(name = "FooterMsg2", length = 500)
    private String footerMsg2;

    @Column(name = "No_Of_Bills", nullable = false)
    private Integer noOfBills;

    @Column(name = "BillType", length = 100, nullable = false)
    private String billType;

    @Column(name = "BillColumn", length = 100, nullable = false)
    private String billColumn;

    @Column(name = "POSQty")
    private Integer posQty;

    @Column(name = "POSTax", length = 200, nullable = false)
    private String posTax;

    @Column(name = "NegativeStock", nullable = false)
    private Boolean negativeStock;

    @Column(name = "MultiMRP", nullable = false)
    private Boolean multiMrp;

    @Column(name = "PCode_Prefix", length = 50)
    private String pcodePrefix;

    @Column(name = "PCode_Digits")
    private Integer pcodeDigits;

    @Column(name = "PCode_Auto", nullable = false)
    private Boolean pcodeAuto;

    @Column(name = "RoundOff", length = 100, nullable = false)
    private String roundOff;

    @Column(name = "POSLine1", length = 200)
    private String posLine1;

    @Column(name = "POSLine2", length = 200)
    private String posLine2;

    @Column(name = "POSLine3", length = 200)
    private String posLine3;

    @Column(name = "POSLine4", length = 200)
    private String posLine4;

    @Column(name = "POSLine5", length = 200)
    private String posLine5;

    @Column(name = "SRLine1", length = 200)
    private String srLine1;

    @Column(name = "SRLine2", length = 200)
    private String srLine2;

    @Column(name = "SRLine3", length = 200)
    private String srLine3;

    @Column(name = "SRLine4", length = 200)
    private String srLine4;

    @Column(name = "SRLine5", length = 200)
    private String srLine5;

    @Column(name = "POLine1", length = 200)
    private String poLine1;

    @Column(name = "POLine2", length = 200)
    private String poLine2;

    @Column(name = "POLine3", length = 200)
    private String poLine3;

    @Column(name = "POLine4", length = 200)
    private String poLine4;

    @Column(name = "POLine5", length = 200)
    private String poLine5;

    @Column(name = "SOLine1", length = 200)
    private String soLine1;

    @Column(name = "SOLine2", length = 200)
    private String soLine2;

    @Column(name = "SOLine3", length = 200)
    private String soLine3;

    @Column(name = "SOLine4", length = 200)
    private String soLine4;

    @Column(name = "SOLine5", length = 200)
    private String soLine5;

    @Column(name = "PRLine1", length = 200)
    private String prLine1;

    @Column(name = "PRLine2", length = 200)
    private String prLine2;

    @Column(name = "PRLine3", length = 200)
    private String prLine3;

    @Column(name = "PRLine4", length = 200)
    private String prLine4;

    @Column(name = "PRLine5", length = 200)
    private String prLine5;

    @Column(name = "QOLine1", length = 200)
    private String qoLine1;

    @Column(name = "QOLine2", length = 200)
    private String qoLine2;

    @Column(name = "QOLine3", length = 200)
    private String qoLine3;

    @Column(name = "QOLine4", length = 200)
    private String qoLine4;

    @Column(name = "QOLine5", length = 200)
    private String qoLine5;

    @Column(name = "DCLine1", length = 200)
    private String dcLine1;

    @Column(name = "DCLine2", length = 200)
    private String dcLine2;

    @Column(name = "DCLine3", length = 200)
    private String dcLine3;

    @Column(name = "DCLine4", length = 200)
    private String dcLine4;

    @Column(name = "DCLine5", length = 200)
    private String dcLine5;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 100, nullable = false)
    private String modifiedBy;

    @Column(name = "NumberDigit", nullable = false)
    private Integer numberDigit;

    @Column(name = "BillPrefix", length = 50)
    private String billPrefix;

    @Column(name = "BillNoStart")
    private Long billNoStart;

    @Column(name = "Topslogan", length = 100)
    private String topslogan;

    @Column(name = "CRMPointValue", nullable = false)
    private Float crmPointValue;

    @Column(name = "BankLine1", length = 200)
    private String bankLine1;

    @Column(name = "BankLine2", length = 200)
    private String bankLine2;

    @Column(name = "BankLine3", length = 200)
    private String bankLine3;

    @Column(name = "BankLine4", length = 200)
    private String bankLine4;

    @Column(name = "BankLine5", length = 200)
    private String bankLine5;

    @Column(name = "SaleBillFormat", length = 100)
    private String saleBillFormat;

    @Column(name = "SaleReturnBillFormat", length = 100)
    private String saleReturnBillFormat;

    @Column(name = "PurchaseBillFormat", length = 100)
    private String purchaseBillFormat;

    @Column(name = "PurchaseReturnBillFormat", length = 100)
    private String purchaseReturnBillFormat;

    @Column(name = "PurchaseOrderBillFormat", length = 100)
    private String purchaseOrderBillFormat;

    @Column(name = "SaleOrderBillFormat", length = 100)
    private String saleOrderBillFormat;

    @Column(name = "QuotationBillFormat", length = 100)
    private String quotationBillFormat;

    @Column(name = "DCBillFormat", length = 100)
    private String dcBillFormat;

    @Column(name = "EstimateBillFormat", length = 100)
    private String estimateBillFormat;

    @Column(name = "StockTransferBillFormat", length = 100)
    private String stockTransferBillFormat;

    @Column(name = "EstimateFullName", length = 200)
    private String estimateFullName;

    @Column(name = "EstimateCompanyName", length = 200)
    private String estimateCompanyName;

    @Column(name = "EstimateAddress1", length = 200)
    private String estimateAddress1;

    @Column(name = "EstimateAddress2", length = 200)
    private String estimateAddress2;

    @Column(name = "EstimateCity", length = 200)
    private String estimateCity;

    @Column(name = "EstimatePhoneNo", length = 200)
    private String estimatePhoneNo;

    @Column(name = "QuotoPerfix", length = 50)
    private String quotoPerfix;

    @Column(name = "QuotoDigit")
    private Integer quotoDigit;

    @Column(name = "DCPerfix", length = 50)
    private String dcPerfix;

    @Column(name = "DCDigit")
    private Integer dcDigit;

    @Column(name = "POPerfix", length = 50)
    private String poPerfix;

    @Column(name = "PODigit")
    private Integer poDigit;

    @Column(name = "SOPerfix", length = 50)
    private String soPerfix;

    @Column(name = "SODigit")
    private Integer soDigit;

    @Column(name = "access_token", length = 2000)
    private String accessToken;

    @Column(name = "expires_in", length = 100)
    private String expiresIn;

    @Column(name = "token_type", length = 100)
    private String tokenType;

    @Column(name = "scope", length = 100)
    private String scope;

    @Column(name = "ExpiryDateTimeUtc", length = 100)
    private String expiryDateTimeUtc;
}
