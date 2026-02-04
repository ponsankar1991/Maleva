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

import java.time.LocalDateTime;

/**
 * JPA entity for DoMaster table
 */
@Entity
@Table(name = "DoMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoMaster {

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

    @Column(name = "CustomerRefId", nullable = false)
    private Integer customerRefId;

    @Column(name = "JobMasterRefId", nullable = false)
    private Integer jobMasterRefId;

    @Column(name = "AgentCompanyRefId")
    private Integer agentCompanyRefId;

    @Column(name = "AgentMasterRefId")
    private Integer agentMasterRefId;

    @Column(name = "SaleDate", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "BillType", length = 50, nullable = false)
    private String billType;

    @Column(name = "SaleType", length = 50, nullable = false)
    private String saleType;

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

    @Column(name = "Offvesselname", length = 200)
    private String offvesselname;

    @Column(name = "Loadingvesselname", length = 200)
    private String loadingvesselname;

    @Column(name = "SPort", length = 200)
    private String sport;

    @Column(name = "Vessel", length = 200)
    private String vessel;

    @Column(name = "Commodity", length = 200)
    private String commodity;

    @Column(name = "ETA")
    private LocalDateTime eta;

    @Column(name = "ETB")
    private LocalDateTime etb;

    @Column(name = "ETD")
    private LocalDateTime etd;

    @Column(name = "DOCNo")
    private Integer docNo;

    @Column(name = "InvoiceNo")
    private Integer invoiceNo;

    @Column(name = "TruckRefid")
    private Integer truckRefid;

    @Column(name = "DriverRefid")
    private Integer driverRefid;

    @Column(name = "AWBNo", length = 100)
    private String awbNo;

    @Column(name = "BLCopy", length = 100)
    private String blCopy;

    @Column(name = "Quantity", length = 100)
    private String quantity;

    @Column(name = "TotalWeight", length = 100)
    private String totalWeight;

    @Column(name = "JStatus")
    private Integer jStatus;

    @Column(name = "OStatus")
    private Integer oStatus;

    @Column(name = "ForkliftbyRefid")
    private Integer forkliftbyRefid;

    @Column(name = "SealbyRefid")
    private Integer sealbyRefid;

    @Column(name = "SealbreakbyRefid")
    private Integer sealbreakbyRefid;

    @Column(name = "PickupDate")
    private LocalDateTime pickupDate;

    @Column(name = "DeliveryDate")
    private LocalDateTime deliveryDate;

    @Column(name = "PickupAddress", length = 1000)
    private String pickupAddress;

    @Column(name = "DeliveryAddress", length = 1000)
    private String deliveryAddress;

    @Column(name = "Forwarding", length = 50)
    private String forwarding;

    @Column(name = "Origin", length = 200)
    private String origin;

    @Column(name = "Destination", length = 200)
    private String destination;

    @Column(name = "Zb", length = 50)
    private String zb;

    @Column(name = "OETA")
    private LocalDateTime oeta;

    @Column(name = "OETB")
    private LocalDateTime oetb;

    @Column(name = "OETD")
    private LocalDateTime oetd;

    @Column(name = "OAgentCompanyRefId")
    private Integer oAgentCompanyRefId;

    @Column(name = "OAgentMasterRefId")
    private Integer oAgentMasterRefId;

    @Column(name = "DODescription", length = 500)
    private String doDescription;

    @Column(name = "SaleOrderMasterNo")
    private Integer saleOrderMasterNo;
}
