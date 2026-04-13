package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for invoice check data
 * Maps to legacy CheckSaleInvoiceCount API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCheckDto {

    @JsonProperty("invoices")
    private List<InvoiceCheckItemDto> invoices;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceCheckItemDto {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("Remarks")
        private String remarks;

        @JsonProperty("JobMasterRefId")
        private Integer jobMasterRefId;

        @JsonProperty("EmployeeName")
        private String employeeName;

        @JsonProperty("Offvesselname")
        private String offVesselName;

        @JsonProperty("Loadingvesselname")
        private String loadingVesselName;

        @JsonProperty("SPort")
        private String sourcePort;

        @JsonProperty("OPort")
        private String originPort;

        @JsonProperty("BillDate")
        private String billDate;

        @JsonProperty("SETA")
        private String sETA;

        @JsonProperty("SETB")
        private String sETB;

        @JsonProperty("SOETA")
        private String sOETA;

        @JsonProperty("SOETB")
        private String sOETB;

        @JsonProperty("SPickupDate")
        private String sPickupDate;

        @JsonProperty("BillNoDisplay")
        private String billNoDisplay;

        @JsonProperty("BillTime")
        private String billTime;

        @JsonProperty("CustomerName")
        private String customerName;

        @JsonProperty("NetAmt")
        private Double netAmt;

        @JsonProperty("SaleType")
        private String saleType;

        @JsonProperty("BillNo")
        private String billNo;

        @JsonProperty("JobStatus")
        private String jobStatus;

        @JsonProperty("InvoiceNo")
        private String invoiceNo;

        @JsonProperty("QNECode")
        private String qneCode;

        @JsonProperty("QNEId")
        private String qneId;

        @JsonProperty("DayCount")
        private Integer dayCount;
    }
}
