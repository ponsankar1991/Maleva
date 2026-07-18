package my.maleva.api.module.saleorder.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SaleOrderInvoiceCheckDto {
    private Integer id;
    private String remarks;
    private Integer jobMasterRefId;
    private String employeeName;
    private String offvesselname;
    private String loadingvesselname;
    private String sPort;
    private String oPort;
    private String billDate;
    private LocalDateTime eta;
    private String seta;
    private String setb;
    private String soeta;
    private String soetb;
    private String sPickupDate;
    private String billNoDisplay;
    private String billTime;
    private String customerName;
    private Double netAmt;
    private String saleType;
    private Integer billNo;
    private String jobStatus;
    private String invoiceNo;
    private String qneCode;
    private String qneId;
    private Integer dayCount;
}
