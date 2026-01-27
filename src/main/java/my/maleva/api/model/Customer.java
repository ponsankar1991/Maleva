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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity for the Customer table.
 * Assumption: table name is `Customer` — change @Table(name="Customer") if your actual table name differs.
 */
@Entity
@Table(name = "Customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "CustomerName", length = 500, nullable = false)
    private String customerName;

    @Column(name = "CNumberDisplay", length = 300, nullable = false)
    private String cNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    @Column(name = "Address1", length = 300)
    private String address1;

    @Column(name = "Address2", length = 300)
    private String address2;

    @Column(name = "City", length = 100)
    private String city;

    @Column(name = "Zipcode", length = 50)
    private String zipcode;

    @Column(name = "Country", length = 50)
    private String country;

    @Column(name = "SymbolRefid", nullable = false)
    private Integer symbolRefid;

    @Column(name = "PaymentTermsRefid", nullable = false)
    private Integer paymentTermsRefid;

    @Column(name = "GSTNO", length = 100)
    private String gstNo;

    @Column(name = "Email", length = 100)
    private String email;

    @Column(name = "MobileNo", length = 50)
    private String mobileNo;

    @Column(name = "UserName", length = 50)
    private String userName;

    @Column(name = "Password", length = 50)
    private String password;

    @Column(name = "Latitude", length = 50)
    private String latitude;

    @Column(name = "longitude", length = 50)
    private String longitude;

    @Column(name = "TokenId", length = 500)
    private String tokenId;

    @Column(name = "OEmail", length = 100)
    private String oEmail;

    @Column(name = "OName", length = 500)
    private String oName;

    @Column(name = "OPhone", length = 50)
    private String oPhone;

    @Column(name = "AEmail", length = 100)
    private String aEmail;

    @Column(name = "AName", length = 500)
    private String aName;

    @Column(name = "APhone", length = 50)
    private String aPhone;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "AEmail1", length = 100)
    private String aEmail1;

    @Column(name = "OEmail1", length = 100)
    private String oEmail1;

    @Column(name = "State", length = 100)
    private String state;

    @Column(name = "Address3", length = 300)
    private String address3;

    @Column(name = "PersonId", length = 100)
    private String personId;

    @Column(name = "OpeningBalance", precision = 19, scale = 4)
    private BigDecimal openingBalance;

    @Column(name = "AccountRefid", nullable = false)
    private Integer accountRefid;

    @Column(name = "TinNo", length = 100)
    private String tinNo;

    @Column(name = "SSTNo", length = 100)
    private String sstNo;

    @Column(name = "MsicCode", length = 100)
    private String msicCode;

    @Column(name = "ServiceTaxType", length = 100)
    private String serviceTaxType;

    @Column(name = "BankName", length = 100)
    private String bankName;

    @Column(name = "AccountNo", length = 100)
    private String accountNo;

    @Column(name = "CompanyCode", length = 50)
    private String companyCode;

    @Column(name = "UpdateId", length = 50)
    private String updateId;

    @Column(name = "Tintype", length = 250)
    private String tintype;

    @Column(name = "CustomerTin", length = 500)
    private String customerTin;

    @Column(name = "eInvoice", length = 500)
    private String eInvoice;

    @Column(name = "ExemptionNo", length = 250)
    private String exemptionNo;

    @Column(name = "ExpiryDate")
    private LocalDate expiryDate;

    @Column(name = "ExemptionDetails", columnDefinition = "nvarchar(max)")
    private String exemptionDetails;

    @Column(name = "RegistrationNo", length = 250)
    private String registrationNo;

    @Column(name = "CustomerCity", length = 100)
    private String customerCity;

    @Column(name = "countryId")
    private Integer countryId;
}
