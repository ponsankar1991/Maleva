package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PaymentVoucher")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucher {

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

    @Column(name = "PayToId", nullable = false)
    private Integer payToId;

    @Column(name = "BillToId")
    private Integer billToId;

    @Column(name = "PaymentVoucherDate", nullable = false)
    private LocalDateTime paymentVoucherDate;

    @Column(name = "BankRefId", nullable = false)
    private Integer bankRefId;

    @Column(name = "RefNumber", length = 100)
    private String refNumber;

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

    @Column(name = "FilePath", length = 3000)
    private String filePath;

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

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "PaymentRefId")
    private Integer paymentRefId;

    @Column(name = "ClaimRefId")
    private Integer claimRefId;

    @Column(name = "SalaryRefId")
    private Integer salaryRefId;

    @Column(name = "ExpenseRefId")
    private Integer expenseRefId;

    @Column(name = "RenewalRefId")
    private Integer renewalRefId;

    @Column(name = "PayStatus", length = 50, nullable = false)
    private String payStatus;

    @Column(name = "PayType", nullable = false)
    private Integer payType;
}
