package my.maleva.api.module.paymentrecept.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

/**
 * Receipt Entity
 * Represents a receipt master record in the system
 * Implements the SP_Receipt stored procedure logic
 */
@Entity
@Table(name = "Receipt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId")
    private Integer companyRefId;

    @Column(name = "UserRefId")
    private Integer userRefId;

    @Column(name = "EmployeeRefId")
    private Integer employeeRefId;

    @Column(name = "LastEmployeeRefId")
    private Integer lastEmployeeRefId;

    @Column(name = "CNumber", nullable = false)
    private Integer CNumber;

    @Column(name = "CNumberDisplay", length = 300, nullable = false)
    private String CNumberDisplay;

    @Column(name = "CustomerRefId", nullable = false)
    private Integer customerRefId;

    @Column(name = "ReceiptDate", nullable = false)
    private LocalDateTime receiptDate;

    @Column(name = "Amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", length = 200, nullable = false)
    private String createdBy;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 200, nullable = false)
    private String modifiedBy;

    @Column(name = "BankRefId", nullable = false)
    private Integer bankRefId;

    @Column(name = "RefNumber", length = 100)
    private String refNumber;

    @Column(name = "PVStatus", nullable = false)
    private Integer pvStatus;

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

    @Column(name = "CurrencyValue")
    private Double currencyValue;

    @Column(name = "ActualNetAmount")
    private Double actualNetAmount;

    @Column(name = "BankCharges")
    private Double bankCharges;

    @Column(name = "name")
    private Integer name;

    @Column(name = "Fileupload")
    private Integer fileUpload;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReceiptDetails> receiptDetails;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (pvStatus == null) {
            pvStatus = 0;
        }
        if (currencyValue == null) {
            currencyValue = 0.0;
        }
        if (actualNetAmount == null) {
            actualNetAmount = 0.0;
        }
        if (bankCharges == null) {
            bankCharges = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}

