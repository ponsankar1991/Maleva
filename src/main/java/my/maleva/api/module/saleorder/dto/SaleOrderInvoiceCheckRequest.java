package my.maleva.api.module.saleorder.dto;

import lombok.Data;

@Data
public class SaleOrderInvoiceCheckRequest {
    private Integer soId;
    private Integer comid;
    private Integer billId;
    private String fromdate;
    private String todate;
    private String reportdate;
    private String category;
    private String portName;
    private Integer id;
    private Integer dId;
    private Integer tId;
    private Integer dashboardStatus;
    private Integer employeeid;
    private Integer statusid;
    private Integer jId;
    private Integer id1;
    private Boolean completestatusnotshow;
    private String search;
    private String vessalNameSearch;
    private Integer rtiMasterRefId;
    private String offvesselname;
    private String loadingvesselname;
    private String status;
    private String statusList;
    private Integer remarks;
    private Boolean eta;
    private Integer etaType;
    private Boolean pickup;
    private String orderBy;
    private Boolean invoice;
    private Boolean invoicecheck;
    private Boolean jobStatus;
    private Integer westport;
}
