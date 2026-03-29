package my.maleva.api.module.invoice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SaleMasterViewModel - Response DTO for sale order master data
 * Maps to the .NET SaleMasterViewModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleMasterViewModel {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("sportsaleorderid")
    private Integer sportsaleorderid;

    @JsonProperty("InvoiceId")
    private Integer invoiceId;

    @JsonProperty("Remarks")
    private String remarks;

    @JsonProperty("Destination")
    private String destination;

    @JsonProperty("FlighTime")
    private String flighTime;

    @JsonProperty("Origin")
    private String origin;

    @JsonProperty("JobMasterRefId")
    private Integer jobMasterRefId;

    @JsonProperty("EmployeeName")
    private String employeeName;

    @JsonProperty("Offvesselname")
    private String offvesselname;

    @JsonProperty("Sname")
    private String sname;

    @JsonProperty("Loadingvesselname")
    private String loadingvesselname;

    @JsonProperty("SPort")
    private String sPort;

    @JsonProperty("OPort")
    private String oPort;

    @JsonProperty("BillDate")
    private String billDate;

    @JsonProperty("DETA")
    private String deta;

    @JsonProperty("ETA")
    private LocalDateTime eta;

    @JsonProperty("SETA")
    private String seta;

    @JsonProperty("SETB")
    private String setb;

    @JsonProperty("SOETA")
    private String soeta;

    @JsonProperty("SOETB")
    private String soetb;

    @JsonProperty("SPickupDate")
    private String sPickupDate;

    @JsonProperty("BillNoDisplay")
    private String billNoDisplay;

    @JsonProperty("BillTime")
    private String billTime;

    @JsonProperty("CustomerName")
    private String customerName;

    @JsonProperty("JobType")
    private String jobType;

    @JsonProperty("NetAmt")
    private Double netAmt;

    @JsonProperty("SaleType")
    private String saleType;

    @JsonProperty("BillNo")
    private Integer billNo;

    @JsonProperty("JobStatus")
    private String jobStatus;

    @JsonProperty("InvoiceNo")
    private String invoiceNo;

    @JsonProperty("QNECode")
    private String qneCode;

    @JsonProperty("QNEId")
    private String qneId;
}

