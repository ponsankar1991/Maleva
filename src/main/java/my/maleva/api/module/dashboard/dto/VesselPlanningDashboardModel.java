package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * VesselPlanningDashboardModel - Response model for Vessel Planning data
 * Maps the complex SQL result set with all vessel planning details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VesselPlanningDashboardModel {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("saleOrderMasterRefId")
    private Integer saleOrderMasterRefId;

    @JsonProperty("origin")
    private String origin;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("jobNo")
    private String jobNo;

    @JsonProperty("boatCPop")
    private Integer boatCPop;

    @JsonProperty("permitCPop")
    private Integer permitCPop;

    @JsonProperty("forwardingCPop")
    private Integer forwardingCPop;

    @JsonProperty("portCPop")
    private Integer portCPop;

    @JsonProperty("liveCPop")
    private Integer liveCPop;

    @JsonProperty("mmheCPop")
    private Integer mmheCPop;

    @JsonProperty("afpoCPop")
    private Integer afpoCPop;

    @JsonProperty("ppFpoCPop")
    private Integer ppFpoCPop;

    @JsonProperty("sfEWpoCPop")
    private Integer sfEWpoCPop;

    @JsonProperty("sfWpoCPop")
    private Integer sfWpoCPop;

    @JsonProperty("boatCPop1")
    private Integer boatCPop1;

    @JsonProperty("pfPPCPop1")
    private Integer pfPPCPop1;

    @JsonProperty("oscn")
    private String oscn;

    @JsonProperty("lscn")
    private String lscn;

    @JsonProperty("vesselType")
    private String vesselType;

    @JsonProperty("jobDate")
    private String jobDate;

    @JsonProperty("jobStatus")
    private String jobStatus;

    @JsonProperty("deta")
    private String deta;

    @JsonProperty("seta")
    private String seta;

    @JsonProperty("eta")
    private String eta;

    @JsonProperty("setb")
    private String setb;

    @JsonProperty("etb")
    private String etb;

    @JsonProperty("setd")
    private String setd;

    @JsonProperty("etd")
    private String etd;

    @JsonProperty("soeta")
    private String soeta;

    @JsonProperty("oeta")
    private String oeta;

    @JsonProperty("soetb")
    private String soetb;

    @JsonProperty("oetb")
    private String oetb;

    @JsonProperty("soetd")
    private String soetd;

    @JsonProperty("oetd")
    private String oetd;

    @JsonProperty("pickupDate")
    private String pickupDate;

    @JsonProperty("sPickupDate")
    private String sPickupDate;

    @JsonProperty("deliveryDate")
    private String deliveryDate;

    @JsonProperty("sDeliveryDate")
    private String sDeliveryDate;

    @JsonProperty("wareHouseEnterDate")
    private String wareHouseEnterDate;

    @JsonProperty("sWareHouseEnterDate")
    private String sWareHouseEnterDate;

    @JsonProperty("wareHouseExitDate")
    private String wareHouseExitDate;

    @JsonProperty("sWareHouseExitDate")
    private String sWareHouseExitDate;

    @JsonProperty("wareHouseAddress")
    private String wareHouseAddress;

    @JsonProperty("pkg")
    private String pkg;

    @JsonProperty("loadingVesselName")
    private String loadingVesselName;

    @JsonProperty("blCopy")
    private String blCopy;

    @JsonProperty("truckSize")
    private String truckSize;

    @JsonProperty("scn")
    private String scn;

    @JsonProperty("port")
    private String port;

    @JsonProperty("sPort")
    private String sPort;

    @JsonProperty("oPort")
    private String oPort;

    @JsonProperty("offVesselName")
    private String offVesselName;

    @JsonProperty("commodity")
    private String commodity;

    @JsonProperty("vessel")
    private String vessel;

    @JsonProperty("oVessel")
    private String oVessel;

    @JsonProperty("agentCompany")
    private String agentCompany;

    @JsonProperty("oAgentCompany")
    private String oAgentCompany;

    @JsonProperty("jobName")
    private String jobName;

    @JsonProperty("awbNo")
    private String awbNo;

    @JsonProperty("remarks1")
    private String remarks1;

    @JsonProperty("cargo")
    private String cargo;

    @JsonProperty("ptw")
    private String ptw;

    @JsonProperty("zb")
    private String zb;

    @JsonProperty("zb2")
    private String zb2;

    @JsonProperty("zbRef")
    private String zbRef;

    @JsonProperty("zbRef2")
    private String zbRef2;

    @JsonProperty("portCharges")
    private String portCharges;

    @JsonProperty("portChargesRef")
    private String portChargesRef;

    @JsonProperty("agentName")
    private String agentName;

    @JsonProperty("agentPhone")
    private String agentPhone;

    @JsonProperty("oAgentName")
    private String oAgentName;

    @JsonProperty("oAgentPhone")
    private String oAgentPhone;

    @JsonProperty("boardingOfficerRefId")
    private Integer boardingOfficerRefId;

    @JsonProperty("boardingOfficerName")
    private String boardingOfficerName;

    @JsonProperty("boardingOfficer1RefId")
    private Integer boardingOfficer1RefId;

    @JsonProperty("boardingOfficerName1")
    private String boardingOfficerName1;

    @JsonProperty("boardingAmount")
    private Double boardingAmount;

    @JsonProperty("boardingAmount1")
    private Double boardingAmount1;

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("sdId")
    private Integer sdId;
}

