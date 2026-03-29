package my.maleva.api.module.supplier.entity;

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
 * Supplier Entity
 * JPA entity for Supplier table
 * Represents supplier/vendor information with comprehensive details
 */
@Entity
@Table(name = "Supplier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "SupplierName", nullable = false, length = 500)
    private String supplierName;

    @Column(name = "CNumberDisplay", nullable = false, length = 300)
    private String cNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    @Column(name = "Address1", length = 300)
    private String address1;

    @Column(name = "Address2", length = 300)
    private String address2;

    @Column(name = "Address3", length = 300)
    private String address3;

    @Column(name = "City", length = 100)
    private String city;

    @Column(name = "SupplierCity", length = 100)
    private String supplierCity;

    @Column(name = "State", length = 100)
    private String state;

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

    @Column(name = "OEmail", length = 100)
    private String oEmail;

    @Column(name = "OEmail1", length = 100)
    private String oEmail1;

    @Column(name = "AEmail", length = 100)
    private String aEmail;

    @Column(name = "AEmail1", length = 100)
    private String aEmail1;

    @Column(name = "MobileNo", length = 50)
    private String mobileNo;

    @Column(name = "OPhone", length = 50)
    private String oPhone;

    @Column(name = "APhone", length = 50)
    private String aPhone;

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

    @Column(name = "OName", length = 500)
    private String oName;

    @Column(name = "AName", length = 500)
    private String aName;

    @Column(name = "PersonId", length = 100)
    private String personId;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", nullable = false, length = 50)
    private String modifiedBy;

    @Column(name = "OpeningBalance")
    private BigDecimal openingBalance;

    @Column(name = "SupplierType", nullable = false, length = 100)
    private String supplierType;

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

    @Column(name = "QNECode", length = 50)
    private String qneCode;

    @Column(name = "QNEId", length = 50)
    private String qneId;

    @Column(name = "SelfBilled")
    private Integer selfBilled;

    @Column(name = "TinType", length = 250)
    private String tinType;

    @Column(name = "SupplierTin", length = 250)
    private String supplierTin;

    @Column(name = "MSICCodeRefId")
    private Integer msicCodeRefId;

    @Column(name = "TaxExemptionNo", length = 250)
    private String taxExemptionNo;

    @Column(name = "ExpiryDate")
    private LocalDate expiryDate;

    @Column(name = "TaxExemptionDetails", length = 500)
    private String taxExemptionDetails;

    @Column(name = "RegistrationNo", length = 250)
    private String registrationNo;
}

