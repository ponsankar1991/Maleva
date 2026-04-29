package my.maleva.api.module.transaction.dto;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Response model for Pre-Alert Report data
 * Equivalent to PreAlertReportModel in C# implementation
 * Contains detailed sale order and pre-alert information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PreAlertReportModel {

    // Sale Order Master Information
    @JsonProperty("saleOrderMasterRefId")
    private Integer saleOrderMasterRefId;

    @JsonProperty("saleDate")
    private LocalDateTime saleDate;

    @JsonProperty("jobNo")
    private String jobNo; // CNumberDisplay

    // Vessel Information
    @JsonProperty("loadingVesselName")
    private String loadingVesselName;

    @JsonProperty("offVesselName")
    private String offVesselName;

    @JsonProperty("vessel")
    private String vessel;

    @JsonProperty("oVessel")
    private String oVessel;

    // Cargo Information
    @JsonProperty("commodity")
    private String commodity;

    @JsonProperty("scn")
    private String scn; // Shipping Control Number

    @JsonProperty("lscn")
    private String lscn; // Loading SCN

    @JsonProperty("awbNo")
    private String awbNo;

    @JsonProperty("truckSize")
    private String truckSize;

    @JsonProperty("totalWeight")
    private BigDecimal totalWeight;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("blCopy")
    private String blCopy;

    // Location Information
    @JsonProperty("origin")
    private String origin;

    @JsonProperty("destination")
    private String destination;

    @JsonProperty("sPort")
    private String sPort; // Source Port

    @JsonProperty("oPort")
    private String oPort; // Origin Port

    // Dates
    @JsonProperty("pickupDate")
    private LocalDateTime pickupDate;

    @JsonProperty("deliveryDate")
    private LocalDateTime deliveryDate;

    @JsonProperty("eta")
    private LocalDateTime eta; // Estimated Time of Arrival

    @JsonProperty("etb")
    private LocalDateTime etb; // Estimated Time of Berth

    @JsonProperty("etd")
    private LocalDateTime etd; // Estimated Time of Departure

    @JsonProperty("oeta")
    private LocalDateTime oeta; // Original ETA

    @JsonProperty("oetb")
    private LocalDateTime oetb; // Original ETB

    @JsonProperty("oetd")
    private LocalDateTime oetd; // Original ETD

    @JsonProperty("deta")
    private LocalDateTime deta; // Display ETA (used for sorting)

    // Job Information
    @JsonProperty("jobTypeMasterRefId")
    private Integer jobTypeMasterRefId;

    @JsonProperty("jobName")
    private String jobName;

    @JsonProperty("jobStatusMasterRefId")
    private Integer jobStatusMasterRefId;

    @JsonProperty("jobStatus")
    private String jobStatus;

    // Employee/Boarding Officer Information
    @JsonProperty("employeeMasterRefId")
    private Integer employeeMasterRefId;

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("boardingOfficerName")
    private String boardingOfficerName;

    @JsonProperty("boardingOfficerName1")
    private String boardingOfficerName1;

    // Agent Information
    @JsonProperty("agentRefId")
    private Integer agentRefId;

    @JsonProperty("agentName")
    private String agentName;

    @JsonProperty("agentPhone")
    private String agentPhone;

    @JsonProperty("oAgentName")
    private String oAgentName; // Origin Agent

    @JsonProperty("oAgentPhone")
    private String oAgentPhone; // Origin Agent Phone

    // Customer Information
    @JsonProperty("customerMasterRefId")
    private Integer customerMasterRefId;

    @JsonProperty("customerName")
    private String customerName;

    // Pre-Alert Information
    @JsonProperty("paRefId")
    private Integer paRefId;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("boardingOfficerNameFromPA")
    private String boardingOfficerNameFromPA;
}

