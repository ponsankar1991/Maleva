package my.maleva.api.module.salecreditmaster.entity;

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
 * SaleCreditMaster Entity
 * JPA entity for SaleCreditMaster table
 */
@Entity
@Table(name = "SaleCreditMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleCreditMaster {

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

    @Column(name = "CustomerRefId", nullable = false)
    private Integer customerRefId;

    @Column(name = "SaleDate", nullable = false)
    private LocalDateTime saleDate;

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

    @Column(name = "RefNumber", length = 100)
    private String refNumber;

    @Column(name = "CStatus", nullable = false)
    private Integer cStatus;

    @Column(name = "SaleMasterRefId", nullable = false)
    private Integer saleMasterRefId;

    @Column(name = "CurrencyValue", nullable = false)
    private Double currencyValue;

    @Column(name = "ActualAmount", nullable = false)
    private Double actualAmount;

    @Column(name = "Coinage", nullable = false)
    private Double coinage;

    @Column(name = "GrossAmount", nullable = false)
    private Double grossAmount;

    @Column(name = "TaxAmount", nullable = false)
    private Double taxAmount;

    @Column(name = "DiscountAmount", nullable = false)
    private Double discountAmount;

    @Column(name = "PlusAmount", nullable = false)
    private Double plusAmount;

    @Column(name = "MinusAmount", nullable = false)
    private Double minusAmount;

    @Column(name = "QNECode", length = 50)
    private String qneCode;

    @Column(name = "QNEId", length = 50)
    private String qneId;

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
}

