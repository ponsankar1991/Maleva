package my.maleva.api.module.purchase.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * PurchaseOrderMaster Entity
 * Represents a purchase order master record in the system
 * Implements the SP_PurchaseOrderMaster stored procedure logic
 */
@Entity
@Table(name = "PurchaseOrderMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "UserRefId")
    private Integer userRefId;

    @Column(name = "EmployeeRefId")
    private Integer employeeRefId;

    @Column(name = "SupplierRefId", nullable = false)
    private Integer supplierRefId;

    @Column(name = "SaleDate", nullable = false)
    private LocalDate saleDate;

    @Column(name = "InvoiceNo", length = 100)
    private String invoiceNo;

    @Column(name = "InvoiceDate", nullable = false)
    private LocalDate invoiceDate;

    @Column(name = "CNumberDisplay", length = 300, nullable = false)
    private String CNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer CNumber;

    @Column(name = "Coinage")
    private Double coinage;

    @Column(name = "GrossAmount")
    private Double grossAmount;

    @Column(name = "TaxAmount")
    private Double taxAmount;

    @Column(name = "DiscountAmount")
    private Double discountAmount;

    @Column(name = "PlusAmount")
    private Double plusAmount;

    @Column(name = "MinusAmount")
    private Double minusAmount;

    @Column(name = "Amount")
    private Double amount;

    @Column(name = "Remarks", length = 300)
    private String remarks;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "TruckRefid")
    private Integer truckRefId;

    @Column(name = "DriverRefid")
    private Integer driverRefId;

    @Column(name = "SaleType", length = 50, nullable = false)
    private String saleType;

    @Column(name = "LastEmployeeRefId")
    private Integer lastEmployeeRefId;

    @Column(name = "SaleMasterRefId")
    private Integer saleMasterRefId;

    @Column(name = "PaymentTermsRefid", nullable = false)
    private Integer paymentTermsRefId;

    @Column(name = "PStatus", nullable = false)
    private Integer pStatus;

    @Column(name = "Description", length = 200)
    private String description;

    @Column(name = "CurrencyValue")
    private Double currencyValue;

    @Column(name = "ActualAmount")
    private Double actualAmount;

    @OneToMany(mappedBy = "purchaseOrderMaster", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseOrderDetails> purchaseOrderDetails;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (active == null) {
            active = 0;
        }
        if (pStatus == null) {
            pStatus = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}

