package my.maleva.api.module.patmentvouchmaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PaymentVoucherMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherMaster {

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

    @Column(name = "CNumberDisplay", length = 50, nullable = false)
    private String cNumberDisplay;

    @Column(name = "PayTo", length = 200, nullable = false)
    private String payTo;

    @Column(name = "PaymentById", nullable = false)
    private Integer paymentById;

    @Column(name = "PaymentVoucherDate", nullable = false)
    private LocalDateTime paymentVoucherDate;

    @Column(name = "Description", length = 200, nullable = false)
    private String description;

    @Column(name = "RefNo", length = 200, nullable = false)
    private String refNo;

    @Column(name = "Amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

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

    @Column(name = "PaymentStatus", length = 50, nullable = false)
    private String paymentStatus;

    @Column(name = "BankCharges", nullable = false)
    private Float bankCharges;

    @Column(name = "CurrencyValue", nullable = false)
    private Float currencyValue;

    @Column(name = "ActualAmount", nullable = false)
    private Float actualAmount;

    @Column(name = "QNECode", length = 50)
    private String qneCode;

    @Column(name = "QNEId", length = 50)
    private String qneId;

    @Column(name = "BillsOrderMasterRefId")
    private Integer billsOrderMasterRefId;

    @Column(name = "paymentReceiptid")
    private Integer paymentReceiptid;

    @Column(name = "EInvoiceUid", length = 100)
    private String eInvoiceUid;

    @Column(name = "EInvoiceSUid", length = 100)
    private String eInvoiceSUid;

    @Column(name = "EInvoiceLongId", length = 100)
    private String eInvoiceLongId;

    @Column(name = "EInvoicePushDT")
    private LocalDateTime eInvoicePushDT;

    @Column(name = "EInvoicePushVDT")
    private LocalDateTime eInvoicePushVDT;

    @Column(name = "EInvoiceStatus", length = 50)
    private String eInvoiceStatus;

    @Column(name = "PayFrom", length = 500)
    private String payFrom;
}
