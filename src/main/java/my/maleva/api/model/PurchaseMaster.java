package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PurchaseMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseMaster {

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
    private String cNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

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

    @Column(name = "PurchaseOrderMasterRefId")
    private Integer purchaseOrderMasterRefId;

    @Column(name = "PaymentTermsRefid", nullable = false)
    private Integer paymentTermsRefId;

    @Column(name = "Description", length = 200)
    private String description;

    @Column(name = "CurrencyValue")
    private Double currencyValue;

    @Column(name = "ActualAmount")
    private Double actualAmount;

    @Column(name = "SerialNo", length = 100)
    private String serialNo;

    @Column(name = "BuyEmployeeName")
    private String buyEmployeeName;
}

