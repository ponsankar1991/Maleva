package my.maleva.api.module.invoice.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One grid row on the Sale Invoice view. The JSON names are the legacy
 * column aliases the React grid was written against; the display strings
 * (dates as dd/MM/yyyy, ETAs with time) are formatted by SQL exactly as the
 * old query did, so the screen shows what it always showed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleInvoiceViewRow {

    @JsonProperty("Id")
    private Integer id;
    @JsonProperty("BillNo")
    private Integer billNo;
    @JsonProperty("BillNoDisplay")
    private String billNoDisplay;
    @JsonProperty("BillDate")
    private String billDate;
    @JsonProperty("BillTime")
    private String billTime;
    @JsonProperty("CustomerName")
    private String customerName;
    @JsonProperty("EmployeeName")
    private String employeeName;
    @JsonProperty("JobStatus")
    private String jobStatus;
    @JsonProperty("JobMasterRefId")
    private Integer jobMasterRefId;
    @JsonProperty("JobNo")
    private String jobNo;
    @JsonProperty("SaleOrderMasterNo")
    private Integer saleOrderMasterNo;
    @JsonProperty("SaleType")
    private String saleType;
    @JsonProperty("NetAmt")
    private Double netAmt;
    @JsonProperty("Amount")
    private Double amount;
    @JsonProperty("QNECode")
    private String qneCode;
    @JsonProperty("QNEId")
    private String qneId;
    @JsonProperty("EInvoiceUid")
    private String eInvoiceUid;
    @JsonProperty("Loadingvesselname")
    private String loadingVesselName;
    @JsonProperty("Offvesselname")
    private String offVesselName;
    @JsonProperty("SPort")
    private String sPort;
    @JsonProperty("OPort")
    private String oPort;
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
    /** The ETA the date range matched on; only set in ETA mode. */
    @JsonProperty("DETA")
    private String deta;
}
