package my.maleva.api.module.billing.bill.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of the bill F5 search grid. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillMasterViewDto {

    private Integer id;
    /** QNE document code; empty until the bill is pushed. */
    private String qneCode;
    private String qneId;
    private String employeeName;
    /** dd/MM/yyyy, pre-formatted for the grid. */
    private String billDate;
    private String invoiceNo;
    private String invoiceDate;
    private String billNoDisplay;
    private String billStatus;
    private String billTime;
    private String supplierName;
    private Float netAmt;
    private String saleType;
    private Integer billNo;
    private String truckName;
    private String driverName;
    private String remarks;
}
