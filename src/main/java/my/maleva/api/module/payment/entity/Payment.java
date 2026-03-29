package my.maleva.api.module.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

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
    private Integer cNumber;

    @Column(name = "CNumberDisplay", length = 300, nullable = false)
    private String cNumberDisplay;

    @Column(name = "SupplierRefId", nullable = false)
    private Integer supplierRefId;

    @Column(name = "PaymentDate", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "BankRefId", nullable = false)
    private Integer bankRefId;

    @Column(name = "RefNumber", length = 100)
    private String refNumber;

    @Column(name = "Amount", nullable = false, precision = 19, scale = 4)
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

    @Column(name = "PaymentStatus", length = 50, nullable = false)
    private String paymentStatus;

    @Column(name = "BankCharges", nullable = false)
    private Float bankCharges;

    @Column(name = "CurrencyValue", nullable = false)
    private Float currencyValue;

    @Column(name = "ActualAmount", nullable = false)
    private Float actualAmount;

    @Column(name = "Description", length = 200)
    private String description;

    @Column(name = "PayTo", length = 100)
    private String payTo;
}
