package my.maleva.api.module.vessalplanning.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class VesselPlanningLegacyDtos {

    private VesselPlanningLegacyDtos() {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NumberResponse {
        private String sequenceNumber;
        private Integer companyId;
        private Boolean success;
        private String error;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class F5Request {
        @NotNull(message = "Company ID is required")
        private Integer comid;
        private Integer employeeid;
        private String search;
        private LocalDate fromdate;
        private LocalDate todate;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchRequest {
        @NotNull(message = "Company ID is required")
        private Integer comid;
        private String search;
        private Integer employeeid;
        @NotBlank(message = "From date is required")
        private String fromdate;
        @NotBlank(message = "To date is required")
        private String todate;
        private Integer etaType;
        private Integer statusId;
        
        // Added field to handle DeliveryDone logic
        @JsonProperty("DeliveryDone")
        private boolean deliveryDone;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaveResponse {
        private boolean ok;
        private String message;
        private String name;
        private Integer id;

        public static SaveResponse success(String vesselPlanningNo, Integer id) {
            return SaveResponse.builder()
                    .ok(true)
                    .message("Vessel planning saved successfully")
                    .name(vesselPlanningNo)
                    .id(id)
                    .build();
        }

        public static SaveResponse error(String message) {
            return SaveResponse.builder()
                    .ok(false)
                    .message(message)
                    .build();
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaveRequest {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("CompanyRefId")
        private Integer companyRefId;

        @JsonProperty("UserRefId")
        private Integer userRefId;

        @JsonProperty("EmployeeRefId")
        private Integer employeeRefId;

        @JsonProperty("FDate")
        @JsonFormat(pattern = "yyyy/MM/dd")
        private LocalDate fDate;

        @JsonProperty("TDate")
        @JsonFormat(pattern = "yyyy/MM/dd")
        private LocalDate tDate;

        @JsonProperty("SaleDate")
        @JsonFormat(pattern = "yyyy/MM/dd")
        private LocalDate saleDate;

        @JsonProperty("CNumberDisplay")
        private String cNumberDisplay;

        @JsonProperty("CNumber")
        private Integer cNumber;

        @JsonProperty("Remarks")
        private String remarks;

        @JsonProperty("Search")
        private String search;

        @JsonProperty("SaleDetails")
        @Valid
        private List<SaveDetailRequest> saleDetails;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SaveDetailRequest {
        @JsonProperty("SaleOrderMasterRefId")
        @NotNull(message = "SaleOrderMasterRefId is required")
        private Integer saleOrderMasterRefId;

        @JsonProperty("Remarks")
        private String remarks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MasterViewModel {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("VESSELPLANINGNo")
        private Integer vesselPlanningNo;

        @JsonProperty("VESSELPLANINGNoDisplay")
        private String vesselPlanningNoDisplay;

        @JsonProperty("VESSELPLANINGDate")
        private String vesselPlanningDate;

        @JsonProperty("Remarks")
        private String remarks;

        @JsonProperty("Search")
        private String search;


        @JsonProperty("SFDate")
        private String sFDate;

        @JsonProperty("STDate")
        private String sTDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailsModel {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("SDId")
        private Integer sdId;

        @JsonProperty("VESSELPLANINGMasterRefId")
        private Integer vesselPlanningMasterRefId;

        @JsonProperty("SaleOrderMasterRefId")
        private Integer saleOrderMasterRefId;

        @JsonProperty("Origin")
        private String origin;

        @JsonProperty("Destination")
        private String destination;

        @JsonProperty("JobNo")
        private String jobNo;

        @JsonProperty("JobDate")
        private String jobDate;

        @JsonProperty("JobStatus")
        private String jobStatus;

        @JsonProperty("SCN")
        private String scn;

        @JsonProperty("LSCN")
        private String lscn;

        @JsonProperty("DETA")
        private LocalDateTime deta;

        @JsonProperty("ETA")
        private LocalDateTime eta;

        @JsonProperty("SETA")
        private String seta;

        @JsonProperty("ETB")
        private LocalDateTime etb;

        @JsonProperty("SETB")
        private String setb;

        @JsonProperty("ETD")
        private LocalDateTime etd;

        @JsonProperty("SETD")
        private String setd;

        @JsonProperty("OETA")
        private LocalDateTime oeta;

        @JsonProperty("SOETA")
        private String soeta;

        @JsonProperty("OETB")
        private LocalDateTime oetb;

        @JsonProperty("SOETB")
        private String soetb;

        @JsonProperty("OETD")
        private LocalDateTime oetd;

        @JsonProperty("SOETD")
        private String soetd;

        @JsonProperty("PickupDate")
        private LocalDateTime pickupDate;

        @JsonProperty("SPickupDate")
        private String sPickupDate;

        @JsonProperty("DeliveryDate")
        private LocalDateTime deliveryDate;

        @JsonProperty("SDeliveryDate")
        private String sDeliveryDate;

        @JsonProperty("WareHouseEnterDate")
        private LocalDateTime wareHouseEnterDate;

        @JsonProperty("SWareHouseEnterDate")
        private String sWareHouseEnterDate;

        @JsonProperty("SWareHouseExitDate")
        private String sWareHouseExitDate;

        @JsonProperty("WareHouseExitDate")
        private LocalDateTime wareHouseExitDate;

        @JsonProperty("WareHouseAddress")
        private String wareHouseAddress;

        @JsonProperty("pkg")
        private String pkg;

        @JsonProperty("Loadingvesselname")
        private String loadingvesselname;

        @JsonProperty("BLCopy")
        private String blCopy;

        @JsonProperty("TruckSize")
        private String truckSize;

        @JsonProperty("Offvesselname")
        private String offvesselname;

        @JsonProperty("Commodity")
        private String commodity;

        @JsonProperty("Vessel")
        private String vessel;

        @JsonProperty("OVessel")
        private String oVessel;

        @JsonProperty("SPort")
        private String sPort;

        @JsonProperty("OPort")
        private String oPort;

        @JsonProperty("JobName")
        private String jobName;

        @JsonProperty("AWBNo")
        private String awbNo;

        @JsonProperty("Remarks1")
        private String remarks1;

        @JsonProperty("PTW")
        private String ptw;

        @JsonProperty("ZB")
        private String zb;

        @JsonProperty("ZB2")
        private String zb2;

        @JsonProperty("ZBRef")
        private String zbRef;

        @JsonProperty("ZBRef2")
        private String zbRef2;

        @JsonProperty("PortCharges")
        private Double portCharges;

        @JsonProperty("PortChargesRef")
        private String portChargesRef;

        @JsonProperty("AgentName")
        private String agentName;

        @JsonProperty("AgentPhone")
        private String agentPhone;

        @JsonProperty("OAgentName")
        private String oAgentName;

        @JsonProperty("OAgentPhone")
        private String oAgentPhone;

        @JsonProperty("BoardingOfficerRefid")
        private Integer boardingOfficerRefid;

        @JsonProperty("BoardingOfficerName")
        private String boardingOfficerName;

        @JsonProperty("BoardingOfficer1Refid")
        private Integer boardingOfficer1Refid;

        @JsonProperty("BoardingOfficerName1")
        private String boardingOfficerName1;

        @JsonProperty("LBoardingOfficerRefid")
        private Integer lBoardingOfficerRefid;

        @JsonProperty("LBoardingOfficerName")
        private String lBoardingOfficerName;

        @JsonProperty("LBoardingOfficer1Refid")
        private Integer lBoardingOfficer1Refid;

        @JsonProperty("LBoardingOfficerName1")
        private String lBoardingOfficerName1;

        @JsonProperty("OBoardingOfficerRefid")
        private Integer oBoardingOfficerRefid;

        @JsonProperty("OBoardingOfficerName")
        private String oBoardingOfficerName;

        @JsonProperty("OBoardingOfficer1Refid")
        private Integer oBoardingOfficer1Refid;

        @JsonProperty("OBoardingOfficerName1")
        private String oBoardingOfficerName1;

        @JsonProperty("BoardingAmount")
        private Double boardingAmount;

        @JsonProperty("BoardingAmount1")
        private Double boardingAmount1;

        @JsonProperty("CustomerName")
        private String customerName;

        @JsonProperty("EmployeeName")
        private String employeeName;

        @JsonProperty("Remarks")
        private String remarks;

        @JsonProperty("Cargo")
        private String cargo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class F5View {
        @JsonProperty("salemaster")
        private List<MasterViewModel> salemaster;

        @JsonProperty("saledetails")
        private List<DetailsModel> saledetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EditResponse {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("CompanyRefId")
        private Integer companyRefId;

        @JsonProperty("UserRefId")
        private Integer userRefId;

        @JsonProperty("EmployeeRefId")
        private Integer employeeRefId;

        @JsonProperty("SFDate")
        private String sFDate;

        @JsonProperty("STDate")
        private String sTDate;

        @JsonProperty("SaleDate")
        private String saleDate;

        @JsonProperty("SSaleDate")
        private String sSaleDate;

        @JsonProperty("CNumberDisplay")
        private String cNumberDisplay;

        @JsonProperty("CNumber")
        private Integer cNumber;

        @JsonProperty("Remarks")
        private String remarks;

        @JsonProperty("Search")
        private String search;

        @JsonProperty("Active")
        private Integer active;

        @JsonProperty("Created_Date")
        private String createdDate;

        @JsonProperty("Created_By")
        private String createdBy;

        @JsonProperty("Modified_Date")
        private String modifiedDate;

        @JsonProperty("Modified_By")
        private String modifiedBy;

        @JsonProperty("SaleDetails")
        private List<DetailsModel> saleDetails;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViewRequest {
        @JsonProperty("SoId")
        @NotNull(message = "SoId is required")
        private Integer soId;

        @JsonProperty("Comid")
        @NotNull(message = "Comid is required")
        private Integer comid;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViewModel {
        @JsonProperty("SFDate")
        private String sFDate;

        @JsonProperty("STDate")
        private String sTDate;

        @JsonProperty("SaleDate")
        private LocalDateTime saleDate;

        @JsonProperty("SSaleDate")
        private String sSaleDate;

        @JsonProperty("PlaningNo")
        private String planingNo;

        @JsonProperty("Remarks")
        private String remarks;

        @JsonProperty("JRemarks")
        private String jRemarks;

        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("Origin")
        private String origin;

        @JsonProperty("Destination")
        private String destination;

        @JsonProperty("JobNo")
        private String jobNo;

        @JsonProperty("JobDate")
        private String jobDate;

        @JsonProperty("JobStatus")
        private String jobStatus;

        @JsonProperty("DETA")
        private LocalDateTime deta;

        @JsonProperty("DETB")
        private LocalDateTime detb;

        @JsonProperty("ETA")
        private String eta;

        @JsonProperty("ETB")
        private String etb;

        @JsonProperty("ETD")
        private String etd;

        @JsonProperty("OETA")
        private String oeta;

        @JsonProperty("OETB")
        private String oetb;

        @JsonProperty("OETD")
        private String oetd;

        @JsonProperty("PickupDate")
        private String pickupDate;

        @JsonProperty("DeliveryDate")
        private String deliveryDate;

        @JsonProperty("WareHouseEnterDate")
        private String wareHouseEnterDate;

        @JsonProperty("WareHouseExitDate")
        private String wareHouseExitDate;

        @JsonProperty("WareHouseAddress")
        private String wareHouseAddress;

        @JsonProperty("pkg")
        private String pkg;

        @JsonProperty("Quantity")
        private String quantity;

        @JsonProperty("TotalWeight")
        private String totalWeight;

        @JsonProperty("Loadingvesselname")
        private String loadingvesselname;

        @JsonProperty("BLCopy")
        private String blCopy;

        @JsonProperty("TruckSize")
        private String truckSize;

        @JsonProperty("SCN")
        private String scn;

        @JsonProperty("LSCN")
        private String lscn;

        @JsonProperty("Offvesselname")
        private String offvesselname;

        @JsonProperty("Commodity")
        private String commodity;

        @JsonProperty("Vessel")
        private String vessel;

        @JsonProperty("OVessel")
        private String oVessel;

        @JsonProperty("SPort")
        private String sPort;

        @JsonProperty("OPort")
        private String oPort;

        @JsonProperty("JobName")
        private String jobName;

        @JsonProperty("AWBNo")
        private String awbNo;

        @JsonProperty("Remarks1")
        private String remarks1;

        @JsonProperty("PTW")
        private String ptw;

        @JsonProperty("ZB")
        private String zb;

        @JsonProperty("ZB2")
        private String zb2;

        @JsonProperty("ZBRef")
        private String zbRef;

        @JsonProperty("ZBRef2")
        private String zbRef2;

        @JsonProperty("PortCharges")
        private Double portCharges;

        @JsonProperty("PortChargesRef")
        private String portChargesRef;

        @JsonProperty("AgentName")
        private String agentName;

        @JsonProperty("AgentPhone")
        private String agentPhone;

        @JsonProperty("OAgentName")
        private String oAgentName;

        @JsonProperty("OAgentPhone")
        private String oAgentPhone;

        @JsonProperty("BoardingOfficerName")
        private String boardingOfficerName;

        @JsonProperty("BoardingOfficerName1")
        private String boardingOfficerName1;

        @JsonProperty("BoardingAmount")
        private Double boardingAmount;

        @JsonProperty("BoardingAmount1")
        private Double boardingAmount1;

        @JsonProperty("CustomerName")
        private String customerName;

        @JsonProperty("EmployeeName")
        private String employeeName;

        @JsonProperty("LEmployeeName")
        private String lEmployeeName;

        @JsonProperty("Cargo")
        private String cargo;
    }
}
