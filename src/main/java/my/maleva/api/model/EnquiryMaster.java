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
 * JPA entity for EnquiryMaster table
 */
@Entity
@Table(name = "EnquiryMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnquiryMaster {

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

    @Column(name = "PickupAddress", length = 2000)
    private String pickupAddress;

    @Column(name = "DeliveryAddress", length = 2000)
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

    @Column(name = "SCN", length = 200)
    private String scn;

    @Column(name = "TruckSize", length = 200)
    private String truckSize;

    @Column(name = "LastEmployeeRefId")
    private Integer lastEmployeeRefId;

    @Column(name = "WareHouseEnterDate")
    private LocalDateTime wareHouseEnterDate;

    @Column(name = "WareHouseExitDate")
    private LocalDateTime wareHouseExitDate;

    @Column(name = "WareHouseAddress", length = 2000)
    private String wareHouseAddress;

    @Column(name = "BoardingOfficerRefid")
    private Integer boardingOfficerRefid;

    @Column(name = "BoardingOfficer1Refid")
    private Integer boardingOfficer1Refid;

    @Column(name = "BoardingAmount", nullable = false)
    private Float boardingAmount;

    @Column(name = "BoardingAmount1", nullable = false)
    private Float boardingAmount1;

    @Column(name = "ForwardingEnterRef", length = 200)
    private String forwardingEnterRef;

    @Column(name = "ForwardingExitRef", length = 200)
    private String forwardingExitRef;

    @Column(name = "PortChargesRef", length = 200)
    private String portChargesRef;

    @Column(name = "PortCharges", nullable = false)
    private Float portCharges;

    @Column(name = "SealAmount", nullable = false)
    private Float sealAmount;

    @Column(name = "BreakSealAmount", nullable = false)
    private Float breakSealAmount;

    @Column(name = "ForwardingEnterRef2", length = 200)
    private String forwardingEnterRef2;

    @Column(name = "ForwardingExitRef2", length = 200)
    private String forwardingExitRef2;

    @Column(name = "ForwardingEnterRef3", length = 200)
    private String forwardingEnterRef3;

    @Column(name = "ForwardingExitRef3", length = 200)
    private String forwardingExitRef3;

    @Column(name = "Forwarding2", length = 50)
    private String forwarding2;

    @Column(name = "Forwarding3", length = 50)
    private String forwarding3;

    @Column(name = "Zb2", length = 50)
    private String zb2;

    @Column(name = "ZbRef", length = 200)
    private String zbRef;

    @Column(name = "ZbRef2", length = 200)
    private String zbRef2;

    @Column(name = "SealAmount2", nullable = false)
    private Float sealAmount2;

    @Column(name = "BreakSealAmount2", nullable = false)
    private Float breakSealAmount2;

    @Column(name = "SealAmount3", nullable = false)
    private Float sealAmount3;

    @Column(name = "BreakSealAmount3", nullable = false)
    private Float breakSealAmount3;

    @Column(name = "SealbyRefid2")
    private Integer sealbyRefid2;

    @Column(name = "SealbreakbyRefid2")
    private Integer sealbreakbyRefid2;

    @Column(name = "SealbyRefid3")
    private Integer sealbyRefid3;

    @Column(name = "SealbreakbyRefid3")
    private Integer sealbreakbyRefid3;

    @Column(name = "LSCN", length = 200)
    private String lscn;

    @Column(name = "Cargo", length = 200)
    private String cargo;

    @Column(name = "PTW", length = 100)
    private String ptw;

    @Column(name = "OVessel", length = 200)
    private String ovessel;

    @Column(name = "OPort", length = 200)
    private String oport;

    @Column(name = "BoardingStartTime")
    private LocalDateTime boardingStartTime;

    @Column(name = "BoardingEndTime")
    private LocalDateTime boardingEndTime;

    @Column(name = "DriverStatus", length = 50)
    private String driverStatus;

    @Column(name = "ForwardingSMKNo", length = 200)
    private String forwardingSMKNo;

    @Column(name = "ForwardingSMKNo2", length = 200)
    private String forwardingSMKNo2;

    @Column(name = "ForwardingSMKNo3", length = 200)
    private String forwardingSMKNo3;

    @Column(name = "CurrencyValue")
    private Float currencyValue;

    @Column(name = "ActualNetAmount")
    private Float actualNetAmount;

    @Column(name = "Remarks1", length = 300)
    private String remarks1;

    @Column(name = "CompletedDate")
    private LocalDateTime completedDate;

    @Column(name = "Forwarding1S1", length = 500)
    private String forwarding1S1;

    @Column(name = "Forwarding1S2", length = 500)
    private String forwarding1S2;

    @Column(name = "Forwarding2S1", length = 500)
    private String forwarding2S1;

    @Column(name = "Forwarding2S2", length = 500)
    private String forwarding2S2;

    @Column(name = "Forwarding3S1", length = 500)
    private String forwarding3S1;

    @Column(name = "Forwarding3S2", length = 500)
    private String forwarding3S2;

    @Column(name = "trucksize2", length = 500)
    private String trucksize2;

    @Column(name = "OriginRefId")
    private Integer originRefId;

    @Column(name = "DestinationRefId")
    private Integer destinationRefId;

    @Column(name = "ForwardingDate")
    private LocalDateTime forwardingDate;

    @Column(name = "Forwarding2Date")
    private LocalDateTime forwarding2Date;

    @Column(name = "Forwarding3Date")
    private LocalDateTime forwarding3Date;

    @Column(name = "JobStatus", length = 200)
    private String jobStatus;
}
