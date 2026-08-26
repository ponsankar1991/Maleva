package my.maleva.api.module.billing.billorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderMasterViewDto {

    private Integer id;
    private Integer pStatus;
    private String employeeName;
    private String billDate;
    private String invoiceNo;
    private String invoiceDate;
    private String billNoDisplay;
    private String billTime;
    private String supplierName;
    private Float netAmt;
    private String saleType;
    private Integer billNo;
    private String truckName;
    private String driverName;
    private String billNoDisplay1;
    private String billStatus;
    private String payTo;
    /** Workshop job order this PO was raised for; null for ordinary purchases. */
    private Integer jobOrderMasterRefId;

    /** Job order number as it reads on screen, e.g. JO000000009. */
    private String jobOrderNo;

    private String description;
    private Integer fileupload;
}



