package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SaleOrderInvoiceCheckModel - Response model for Invoice check data
 * Maps the SQL result for CheckSaleInvoiceCount operation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderInvoiceCheckModel {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("jobMasterRefId")
    private Integer jobMasterRefId;

    @JsonProperty("employeeName")
    private String employeeName;

    @JsonProperty("offVesselName")
    private String offVesselName;

    @JsonProperty("loadingVesselName")
    private String loadingVesselName;

    @JsonProperty("sPort")
    private String sPort;

    @JsonProperty("oPort")
    private String oPort;

    @JsonProperty("billDate")
    private String billDate;

    @JsonProperty("eta")
    private String eta;

    @JsonProperty("seta")
    private String seta;

    @JsonProperty("setb")
    private String setb;

    @JsonProperty("soeta")
    private String soeta;

    @JsonProperty("soetb")
    private String soetb;

    @JsonProperty("sPickupDate")
    private String sPickupDate;

    @JsonProperty("billNoDisplay")
    private String billNoDisplay;

    @JsonProperty("billTime")
    private String billTime;

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("netAmt")
    private Double netAmt;

    @JsonProperty("saleType")
    private String saleType;

    @JsonProperty("billNo")
    private Integer billNo;

    @JsonProperty("jobStatus")
    private String jobStatus;

    @JsonProperty("invoiceNo")
    private String invoiceNo;

    @JsonProperty("qneCode")
    private String qneCode;

    @JsonProperty("qneId")
    private String qneId;

    @JsonProperty("dayCount")
    private Integer dayCount;
}

