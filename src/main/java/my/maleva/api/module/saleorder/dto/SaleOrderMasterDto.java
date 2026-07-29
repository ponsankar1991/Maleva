package my.maleva.api.module.saleorder.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SaleOrderMasterDto - DTO for SaleOrderMaster
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderMasterDto {
    private Integer id;
    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;
    private Integer userRefId;
    private Integer employeeRefId;
    @NotNull(message = "Customer Reference ID is required")
    private Integer customerRefId;
    private String customerName;
    @NotNull(message = "Job Master Reference ID is required")
    private Integer jobMasterRefId;
    private Integer agentCompanyRefId;
    private Integer agentMasterRefId;
    @NotNull(message = "Sale Date is required")
    private LocalDateTime saleDate;
    @NotBlank(message = "Bill Type is required")
    @Size(max = 50)
    private String billType;
    @NotBlank(message = "Sale Type is required")
    @Size(max = 50)
    private String saleType;
    @NotBlank(message = "C Number Display is required")
    @Size(max = 300)
    private String cNumberDisplay;
    @NotNull(message = "C Number is required")
    private Integer cNumber;
    private Double coinage;
    private Double grossAmount;
    private Double taxAmount;
    private Double discountAmount;
    private Double plusAmount;
    private Double minusAmount;
    @NotNull(message = "Amount is required")
    private Double amount;
    @Size(max = 300)
    private String remarks;
    private Integer active;
    private LocalDateTime createdDate;
    @Size(max = 50)
    private String createdBy;
    private LocalDateTime modifiedDate;
    @Size(max = 50)
    private String modifiedBy;
    private String offvesselname;
    private String loadingvesselname;
    private String sPort;
    private String vessel;
    private String commodity;
    private LocalDateTime eta;
    private LocalDateTime etb;
    private LocalDateTime etd;
    private Integer docNo;
    private Integer invoiceNo;
    private Integer truckRefid;
    private Integer driverRefid;
    @Size(max = 100)
    private String awbNo;
    @Size(max = 100)
    private String blCopy;
    @Size(max = 100)
    private String quantity;
    @Size(max = 100)
    private String totalWeight;
    private Integer jStatus;
    private String statusName;
    private Integer oStatus;
    private Integer forkliftbyRefid;
    private Integer sealbyRefid;
    private Integer sealbreakbyRefid;
    private LocalDateTime pickupDate;
    private LocalDateTime deliveryDate;
    private String pickupAddress;
    private String deliveryAddress;
    @Size(max = 50)
    private String forwarding;
    private String origin;
    private String destination;
    @Size(max = 50)
    private String zb;
    private LocalDateTime oeta;
    private LocalDateTime oetb;
    private LocalDateTime oetd;
    private Integer oAgentCompanyRefId;
    private Integer oAgentMasterRefId;
    @Size(max = 500)
    private String doDescription;
    @Size(max = 200)
    private String scn;
    private String truckSize;
    private Integer lastEmployeeRefId;
    private LocalDateTime wareHouseEnterDate;
    private LocalDateTime wareHouseExitDate;
    private String wareHouseAddress;
    private Integer boardingOfficerRefid;
    private Integer boardingOfficer1Refid;
    private Double boardingAmount;
    private Double boardingAmount1;
    private String forwardingEnterRef;
    private String forwardingExitRef;
    private String portChargesRef;
    private Double portCharges;
    private Double sealAmount;
    private Double breakSealAmount;
    private String forwardingEnterRef2;
    private String forwardingExitRef2;
    private String forwardingEnterRef3;
    private String forwardingExitRef3;
    private String forwarding2;
    private String forwarding3;
    private String zb2;
    private String zbRef;
    private String zbRef2;
    private Double sealAmount2;
    private Double breakSealAmount2;
    private Double sealAmount3;
    private Double breakSealAmount3;
    private Integer sealbyRefid2;
    private Integer sealbreakbyRefid2;
    private Integer sealbyRefid3;
    private Integer sealbreakbyRefid3;
    private String lscn;
    private String cargo;
    private String ptw;
    private String oVessel;
    private String oPort;
    private LocalDateTime boardingStartTime;
    private LocalDateTime boardingEndTime;
    private String driverStatus;
    private String forwardingSMKNo;
    private String forwardingSMKNo2;
    private String forwardingSMKNo3;
    private Double currencyValue;
    private Double actualNetAmount;
    private String remarks1;
    private LocalDateTime completedDate;
    private String forwarding1S1;
    private String forwarding1S2;
    private String forwarding2S1;
    private String forwarding2S2;
    private String forwarding3S1;
    private String forwarding3S2;
    private String trucksize2;
    private Integer originRefId;
    private Integer destinationRefId;
    private LocalDateTime forwardingDate;
    private LocalDateTime forwarding2Date;
    private LocalDateTime forwarding3Date;
    private String truckName1;
    private String remarkDetails;
    private String driverName;
    private Integer lBoardingOfficerRefid;
    private Integer lBoardingOfficer1Refid;
    private Double lBoardingAmount;
    private Double lBoardingAmount1;
    private String lPortChargesRef;
    private Double lPortCharges;
    private Integer oBoardingOfficerRefid;
    private Integer oBoardingOfficer1Refid;
    private Double oBoardingAmount;
    private Double oBoardingAmount1;
    private String oPortChargesRef;
    private Double oPortCharges;
    private String lptw;
    private String optw;
    private LocalDateTime flighTime;
    private Integer symbolRefId;
    private String forwardingQuantity;
    private String forwardingQuantity2;
    private String forwardingQuantity3;
    private Integer portCPop;
    private Integer forwardingCPop;
    private Integer boatCPop;
    private Integer permitCPop;
    private String quantityList;
    private Integer livecpop;
    private Integer mmheCPop;
    private Integer afpoCPop;
    private Integer ppFpoCPop;
    private Integer sfewpoCPop;
    private Integer sfWpoCPop;
    private Integer boatCPop1;
    private Integer pfppCPop1;
    private String rbtportchagdeop;
    private String pickuptimelist;
    private String pickupQuantitylist;
    private String deliveryQuantitylist;
    private String delivertimelist;
    private Integer notportchagre;
    private Integer notBoatCPop;
    private Integer notBoatCPop1;
    private Integer notPFPPCPop1;
    private Integer notForwardingCPop;
    private Integer notPermitCPop;
    private Integer notLevyChares;
    private Integer notMMHECPop;
    private Integer notAFpoCPop;
    private Integer notSFWpoCPop;
    private Integer notSFEWpoCPop;
    private String oiDateIn;
    private String odiDateOut;
    private Integer sportsaleorderid;

    @JsonProperty("boardingOfficer2Refid")
    @JsonAlias({"BoardingOfficer2Refid"})
    private Integer boardingOfficer2Refid;

    @JsonProperty("boardingAmount2")
    @JsonAlias({"BoardingAmount2"})
    private String boardingAmount2;

    @JsonProperty("lBoardingOfficer2Refid")
    @JsonAlias({"LBoardingOfficer2Refid"})
    private Integer lBoardingOfficer2Refid;

    @JsonProperty("lBoardingAmount2")
    @JsonAlias({"LBoardingAmount2"})
    private String lBoardingAmount2;

    @JsonProperty("oBoardingOfficer2Refid")
    @JsonAlias({"OBoardingOfficer2Refid"})
    private Integer oBoardingOfficer2Refid;

    @JsonProperty("oBoardingAmount2")
    @JsonAlias({"OBoardingAmount2"})
    private String oBoardingAmount2;
}
