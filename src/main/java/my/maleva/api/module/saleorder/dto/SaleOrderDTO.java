package my.maleva.api.module.saleorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SaleOrderDTO - Comprehensive DTO for Sale Order with all nested details
 * This DTO represents the complete sale order structure with order details, pickup, delivery, and forwarding information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderDTO {

    // Main Order Fields
    private Integer id;

    private Integer spotId;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    private Integer agentCompanyRefId;

    private Integer agentMasterRefId;

    private Integer oAgentCompanyRefId;

    private Integer oAgentMasterRefId;

    @NotNull(message = "Customer Reference ID is required")
    private Integer customerRefId;

    private Integer jobMasterRefId;

    @Size(max = 50)
    private String saleDate;

    @Size(max = 50)
    private String saleType;

    private String cNumberDisplay;

    private Integer cNumber;

    @Size(max = 100)
    private String coinage;

    private Integer sportsaleorderid;

    @Size(max = 100)
    private String grossAmount;

    @Size(max = 100)
    private String taxAmount;

    private Integer discountAmount;

    @Size(max = 2000)
    private String remarks;

    @Size(max = 500)
    private String remarks1;

    // Boolean flags for various charges and options
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
    private Integer portCPop;
    private Integer forwardingCPop;
    private Integer boatCPop;
    private Integer permitCPop;
    private Integer liveCPop;
    private Integer mMHECPop;
    private Integer aFpoCPop;
    private Integer pFPPCPop1;
    private Integer sFWpoCPop;
    private Integer boatCPop1;
    private Integer sFEWpoCPop;

    @Size(max = 100)
    private String rbtportchagdeop;

    private Integer plusAmount;

    private Integer minusAmount;

    @Size(max = 500)
    private String doDescription;

    @Size(max = 100)
    private String amount;

    @Size(max = 200)
    private String offvesselname;

    @Size(max = 200)
    private String loadingvesselname;

    @Size(max = 100)
    private String billType;

    @Size(max = 200)
    private String sPort;

    @Size(max = 200)
    private String oPort;

    @Size(max = 200)
    private String vessel;

    @Size(max = 200)
    private String oVessel;

    private Integer commodity;

    @Size(max = 100)
    private String cargo;

    @Size(max = 100)
    private String eta;

    @Size(max = 100)
    private String etb;

    @Size(max = 100)
    private String etd;

    @Size(max = 100)
    private String oeta;

    @Size(max = 100)
    private String oetb;

    @Size(max = 100)
    private String oetd;

    @Size(max = 100)
    private String forwardingDate;

    @Size(max = 100)
    private String forwarding2Date;

    @Size(max = 100)
    private String forwarding3Date;

    private Integer docNo;

    private Integer invoiceNo;

    private Integer truckRefid;

    private Integer driverRefid;

    @Size(max = 100)
    private String awbNo;

    @Size(max = 100)
    private String ptw;

    @Size(max = 100)
    private String lptw;

    @Size(max = 100)
    private String optw;

    @Size(max = 100)
    private String blCopy;

    @Size(max = 100)
    private String quantity;

    @Size(max = 100)
    private String totalWeight;

    @Size(max = 100)
    private String truckSize;

    private Integer jStatus;

    private Integer oStatus;

    private Integer forkliftbyRefid;

    private Integer sealbyRefid;

    private Integer sealbreakbyRefid;

    private Integer sealbyRefid2;

    private Integer sealbreakbyRefid2;

    private Integer sealbyRefid3;

    private Integer sealbreakbyRefid3;

    private Integer boardingOfficerRefid;

    private Integer boardingOfficer1Refid;

    @Size(max = 100)
    private String boardingAmount;

    @Size(max = 100)
    private String boardingAmount1;

    private Integer lBoardingOfficerRefid;

    private Integer lBoardingOfficer1Refid;

    @Size(max = 100)
    private String lBoardingAmount;

    @Size(max = 100)
    private String lBoardingAmount1;

    private Integer oBoardingOfficerRefid;

    private Integer oBoardingOfficer1Refid;

    @Size(max = 100)
    private String oBoardingAmount;

    @Size(max = 100)
    private String oBoardingAmount1;

    @Size(max = 200)
    private String forwardingEnterRef;

    @Size(max = 200)
    private String forwardingExitRef;

    @Size(max = 200)
    private String forwardingEnterRef2;

    @Size(max = 200)
    private String forwardingExitRef2;

    @Size(max = 200)
    private String forwardingEnterRef3;

    @Size(max = 100)
    private String forwardingQuantity;

    @Size(max = 100)
    private String forwardingQuantity2;

    @Size(max = 100)
    private String forwardingQuantity3;

    @Size(max = 200)
    private String forwardingExitRef3;

    @Size(max = 100)
    private String forwardingSMKNo;

    @Size(max = 100)
    private String forwardingSMKNo2;

    @Size(max = 100)
    private String forwardingSMKNo3;

    @Size(max = 200)
    private String portChargesRef;

    @Size(max = 100)
    private String portCharges;

    @Size(max = 200)
    private String lPortChargesRef;

    @Size(max = 100)
    private String lPortCharges;

    @Size(max = 200)
    private String oPortChargesRef;

    @Size(max = 100)
    private String oPortCharges;

    @Size(max = 100)
    private String sealAmount;

    @Size(max = 100)
    private String breakSealAmount;

    @Size(max = 100)
    private String sealAmount2;

    @Size(max = 100)
    private String breakSealAmount2;

    @Size(max = 100)
    private String sealAmount3;

    @Size(max = 100)
    private String breakSealAmount3;

    @Size(max = 100)
    private String pickupDate;

    @Size(max = 100)
    private String deliveryDate;

    @Size(max = 2000)
    private String pickupAddress;

    @Size(max = 2000)
    private String pickupQuantityList;

    @Size(max = 2000)
    private String deliveryQuantityList;

    @Size(max = 2000)
    private String wareHouseAddress;

    @Size(max = 100)
    private String wareHouseEnterDate;

    @Size(max = 100)
    private String wareHouseExitDate;

    @Size(max = 2000)
    private String quantitylist;

    @Size(max = 2000)
    private String deliveryAddress;

    private Integer forwarding;

    private Integer forwarding2;

    private Integer forwarding3;

    @Size(max = 200)
    private String origin;

    @Size(max = 200)
    private String destination;

    @Size(max = 100)
    private String scn;

    @Size(max = 100)
    private String lscn;

    private Integer zb;

    private Integer zb2;

    @Size(max = 100)
    private String zbRef;

    @Size(max = 100)
    private String zbRef2;

    private Integer forwarding1S1;

    private Integer forwarding1S2;

    private Integer forwarding2S1;

    private Integer forwarding2S2;

    private Integer forwarding3S1;

    private Integer forwarding3S2;

    @Size(max = 100)
    private String trucksize2;

    private Integer originRefId;

    private Integer symbolRefId;

    private Integer destinationRefId;

    private Integer currencyValue;

    @Size(max = 100)
    private String actualNetAmount;

    @Size(max = 100)
    private String flighTime;

    // Nested Collections
    private List<SaleOrderDetailsDto> SaleOrderDetails;

    private List<PickupDetailDTO> pickupDetails;

    private List<DeliveryDetailDTO> deliveryDetails;

    private List<ForwardingDetailDTO> forwardingDetails;
}

