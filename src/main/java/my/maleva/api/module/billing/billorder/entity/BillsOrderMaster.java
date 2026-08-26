package my.maleva.api.module.billing.billorder.entity;

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
 * JPA entity for BillsOrderMaster table
 */
@Entity
@Table(name = "BillsOrderMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderMaster {

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

    @Column(name = "SaleMasterRefId")
    private Integer saleMasterRefId;

    @Column(name = "PaymentTermsRefid", nullable = false)
    private Integer paymentTermsRefid;

    @Column(name = "PStatus")
    private Integer pStatus;

    /**
     * The workshop job order this PO was raised for, when it was.
     *
     * One repair job buys from several vendors, so a job raises several POs -
     * the link lives here rather than on the job so each PO names its own job.
     * Null for every ordinary purchase that has nothing to do with a repair.
     */
    @Column(name = "JobOrderMasterRefId")
    private Integer jobOrderMasterRefId;

    /**
     * The job order's number as it reads on screen, e.g. JO000000009.
     *
     * Stored beside the id on purpose: the PO list shows this on every row, and
     * carrying the text means that list does not have to join JobOrderMaster
     * just to print a number that never changes once the job is raised.
     */
    @Column(name = "JobOrderNo", length = 50)
    private String jobOrderNo;

    @Column(name = "Description", length = 200)
    private String description;

    @Column(name = "CurrencyValue", nullable = false)
    private Float currencyValue;

    @Column(name = "ActualAmount", nullable = false)
    private Float actualAmount;

    @Column(name = "PayTo", length = 250)
    private String payTo;

    @Column(name = "BillStatus", length = 250)
    private String billStatus;

    @Column(name = "DueDate")
    private LocalDate dueDate;

    @Column(name = "fileupload")
    private Integer fileupload;

    @Column(name = "OffVessal", length = 300)
    private String offVessal;

    @Column(name = "LodingVessal", length = 300)
    private String lodingVessal;

    @Column(name = "CheckloadingVessel")
    private Integer checkloadingVessel;

    @Column(name = "CheckoffgVessel")
    private Integer checkoffgVessel;
}
