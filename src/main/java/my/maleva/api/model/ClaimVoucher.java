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
import java.time.LocalDateTime;

/**
 * JPA entity for ClaimVoucher table
 */
@Entity
@Table(name = "ClaimVoucher")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimVoucher {

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

    @Column(name = "SupplierRefId1")
    private Integer supplierRefId1;

    @Column(name = "ClaimVoucherDate", nullable = false)
    private LocalDateTime claimVoucherDate;

    @Column(name = "BankRefId", nullable = false)
    private Integer bankRefId;

    @Column(name = "RefNumber", length = 100)
    private String refNumber;

    @Column(name = "FilePath", length = 3000)
    private String filePath;

    @Column(name = "Amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "Remarks", length = 500)
    private String remarks;

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

    @Column(name = "PVStatus")
    private Integer pvStatus;
}
