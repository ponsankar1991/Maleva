package my.maleva.api.module.transaction.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;

/**
 * Response model for Pre-Alert Report data
 * Equivalent to PreAlertReportModel in C# implementation
 * Contains detailed sale order and pre-alert information
 *
 * Note: Date fields are String to match C# FORMAT() output (dd/MM/yyyy HH:mm:ss)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PreAlertReportModel {

    // Sale Order Master Information
    @JsonAlias({"SaleOrderMasterRefId", "saleOrderMasterRefId"})
    private Integer saleOrderMasterRefId;

    @JsonAlias({"SaleDate", "saleDate"})
    private String saleDate;

    @JsonAlias({"JobNo", "jobNo"})
    private String jobNo;

    // Vessel Information
    @JsonAlias({"Loadingvesselname", "LoadingVesselName", "loadingVesselName"})
    private String loadingVesselName;

    @JsonAlias({"Offvesselname", "OffVesselName", "offVesselName"})
    private String offVesselName;

    @JsonAlias({"Vessel", "vessel"})
    private String vessel;

    @JsonAlias({"OVessel", "oVessel"})
    private String oVessel;

    // Cargo Information
    @JsonAlias({"Commodity", "commodity"})
    private String commodity;

    @JsonAlias({"SCN", "scn"})
    private String scn;

    @JsonAlias({"LSCN", "lscn"})
    private String lscn;

    @JsonAlias({"AWBNo", "awbNo"})
    private String awbNo;

    @JsonAlias({"TruckSize", "truckSize"})
    private String truckSize;

    @JsonAlias({"TotalWeight", "totalWeight"})
    private String totalWeight;

    @JsonAlias({"Quantity", "quantity"})
    private String quantity;

    @JsonAlias({"BLCopy", "blCopy"})
    private String blCopy;

    // Location Information
    @JsonAlias({"Origin", "origin"})
    private String origin;

    @JsonAlias({"Destination", "destination"})
    private String destination;

    @JsonAlias({"SPort", "sPort", "Port"})
    private String sPort;

    @JsonAlias({"OPort", "oPort"})
    private String oPort;

    // Dates - String to match C# FORMAT() output
    @JsonAlias({"PickupDate", "pickupDate"})
    private String pickupDate;

    @JsonAlias({"DeliveryDate", "deliveryDate"})
    private String deliveryDate;

    @JsonAlias({"ETA", "eta"})
    private String eta;

    @JsonAlias({"ETB", "etb"})
    private String etb;

    @JsonAlias({"ETD", "etd"})
    private String etd;

    @JsonAlias({"OETA", "oeta"})
    private String oeta;

    @JsonAlias({"OETB", "oetb"})
    private String oetb;

    @JsonAlias({"OETD", "oetd"})
    private String oetd;

    @JsonAlias({"DETA", "deta"})
    private String deta;

    // Job Information
    @JsonAlias({"JobTypeMasterRefId", "jobTypeMasterRefId"})
    private Integer jobTypeMasterRefId;

    @JsonAlias({"JobName", "jobName"})
    private String jobName;

    @JsonAlias({"JobStatusMasterRefId", "jobStatusMasterRefId"})
    private Integer jobStatusMasterRefId;

    @JsonAlias({"JobStatus", "jobStatus", "Jobstatus"})
    private String jobStatus;

    // Employee/Boarding Officer Information
    @JsonAlias({"EmployeeMasterRefId", "employeeMasterRefId"})
    private Integer employeeMasterRefId;

    @JsonAlias({"EmployeeName", "employeeName"})
    private String employeeName;

    @JsonAlias({"BoardingOfficerName", "boardingOfficerName"})
    private String boardingOfficerName;

    @JsonAlias({"BoardingOfficerRefid", "boardingOfficerRefId"})
    private Integer boardingOfficerRefId;

    @JsonAlias({"BoardingOfficerName1", "boardingOfficerName1"})
    private String boardingOfficerName1;

    // Agent Information
    @JsonAlias({"AgentRefId", "agentRefId"})
    private Integer agentRefId;

    @JsonAlias({"AgentName", "agentName"})
    private String agentName;

    @JsonAlias({"AgentPhone", "agentPhone"})
    private String agentPhone;

    @JsonAlias({"OAgentName", "oAgentName"})
    private String oAgentName;

    @JsonAlias({"OAgentPhone", "oAgentPhone"})
    private String oAgentPhone;

    // Customer Information
    @JsonAlias({"CustomerMasterRefId", "customerMasterRefId"})
    private Integer customerMasterRefId;

    @JsonAlias({"CustomerName", "customerName"})
    private String customerName;

    // Pre-Alert Information
    @JsonAlias({"PARefId", "paRefId", "PARefId"})
    private Integer paRefId;

    @JsonAlias({"Remarks", "remarks"})
    private String remarks;

    @JsonAlias({"BoardingOfficerName", "boardingOfficerNameFromPA"})
    private String boardingOfficerNameFromPA;
}
