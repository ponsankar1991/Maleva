package my.maleva.api.module.planning.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PlanningDetailsModel - Response DTO for planning details data
 * Maps to the .NET PLANINGDetailsModel with comprehensive logistics data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningDetailsModel {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("SDId")
    private Integer sdId;

    @JsonProperty("PLANINGMasterRefId")
    private Integer planningMasterRefId;

    @JsonProperty("SaleOrderMasterRefId")
    private Integer saleOrderMasterRefId;

    @JsonProperty("TruckRefid")
    private Integer truckRefId;

    @JsonProperty("TruckName")
    private String truckName;

    @JsonProperty("DriverName")
    private String driverName;

    @JsonProperty("JobNo")
    private String jobNo;

    @JsonProperty("JobDate")
    private String jobDate;

    @JsonProperty("JobStatus")
    private String jobStatus;

    @JsonProperty("JobName")
    private String jobName;

    @JsonProperty("AWBNo")
    private String awbNo;

    @JsonProperty("BLCopy")
    private String blCopy;

    @JsonProperty("CustomerName")
    private String customerName;

    @JsonProperty("Remarks")
    private String remarks;

    @JsonProperty("Origin")
    private String origin;

    @JsonProperty("Destination")
    private String destination;

    @JsonProperty("OriginD")
    private String originD;

    @JsonProperty("DestinationD")
    private String destinationD;

    @JsonProperty("VesselName")
    private String vesselName;

    @JsonProperty("pkg")
    private String pkg;

    @JsonProperty("EmployeeName")
    private String employeeName;

    @JsonProperty("truckSize")
    private String truckSize;

    @JsonProperty("SPickupDate")
    private String sPickupDate;

    @JsonProperty("PickupDate")
    private LocalDateTime pickupDate;

    @JsonProperty("DeliveryDate")
    private LocalDateTime deliveryDate;

    @JsonProperty("PickupDateD")
    private String pickupDateD;

    @JsonProperty("DeliveryDateD")
    private String deliveryDateD;

    @JsonProperty("LETA")
    private String leta;

    @JsonProperty("OETA")
    private String oeta;

    @JsonProperty("SDeliveryDate")
    private String sDeliveryDate;

    @JsonProperty("WareHouseEnterDate")
    private LocalDateTime wareHouseEnterDate;

    @JsonProperty("WareHouseExitDate")
    private LocalDateTime wareHouseExitDate;

    @JsonProperty("SWareHouseEnterDate")
    private String sWareHouseEnterDate;

    @JsonProperty("SWareHouseExitDate")
    private String sWareHouseExitDate;

    @JsonProperty("WareHouseAddress")
    private String wareHouseAddress;

    @JsonProperty("PickupAddress")
    private String pickupAddress;

    @JsonProperty("DeliveryAddress")
    private String deliveryAddress;

    @JsonProperty("SPort")
    private String sPort;

    @JsonProperty("OPort")
    private String oPort;

    @JsonProperty("SortBy")
    private Integer sortBy;

    @JsonProperty("TruckNameD")
    private String truckNameD;

    @JsonProperty("DriverNameD")
    private String driverNameD;

    @JsonProperty("pickuptimelist")
    private String pickupTimeList;

    @JsonProperty("pickupQuantitylist")
    private String pickupQuantityList;

    @JsonProperty("DeliveryQuantitylist")
    private String deliveryQuantityList;

    @JsonProperty("Delivertimelist")
    private String deliveryTimeList;
}
