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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity for BillMaster table
 */
@Entity
@Table(name = "BillMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillMaster {

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
    private LocalDateTime saleDate;

    @Column(name = "InvoiceNo", length = 100)
    private String invoiceNo;

    @Column(name = "InvoiceDate", nullable = false)
    private LocalDateTime invoiceDate;

    @Column(name = "CNumberDisplay", length = 300, nullable = false)
    private String cNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    @Column(name = "Coinage", nullable = false)
    private Float coinage;

    @Column(name = "GrossAmount", nullable = false)
    private Float grossAmount;

    @Column(name = "TaxAmount", nullable = false)
    private Float taxAmount;

    @Column(name = "DiscountAmount", nullable = false)
    private Float discountAmount;

    @Column(name = "PlusAmount", nullable = false)
    private Float plusAmount;

    @Column(name = "MinusAmount", nullable = false)
    private Float minusAmount;

    @Column(name = "Amount", nullable = false)
    private Float amount;

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
    private Integer truckRefid;

    @Column(name = "DriverRefid")
    private Integer driverRefid;

    @Column(name = "SaleType", length = 50, nullable = false)
    private String saleType;

    @Column(name = "LastEmployeeRefId")
    private Integer lastEmployeeRefId;

    @Column(name = "PaymentTermsRefid", nullable = false)
    private Integer paymentTermsRefid;

    @Column(name = "Description", length = 200)
    private String description;

    @Column(name = "CurrencyValue", nullable = false)
    private Float currencyValue;

    @Column(name = "ActualAmount", nullable = false)
    private Float actualAmount;

    @Column(name = "QNECode", length = 50)
    private String qneCode;

    @Column(name = "QNEId", length = 50)
    private String qneId;

    @Column(name = "CurrencyValue1", nullable = false)
    private Float currencyValue1;

    @Column(name = "DueDate")
    private LocalDate dueDate;

    @Column(name = "BillsOrderMasterRefId")
    private Integer billsOrderMasterRefId;

    @Column(name = "BillStatus", length = 50)
    private String billStatus;
}
