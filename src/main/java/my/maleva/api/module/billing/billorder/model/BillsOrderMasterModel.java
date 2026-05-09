package my.maleva.api.module.billing.billorder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderMasterModel {
    private Integer id;
    private Integer SDId;
    private Integer fileupload;
    private Integer companyRefId;
    private Integer userRefId;
    private Integer employeeRefId;
    private String invoiceNo;
    private Date invoiceDate;
    private String sInvoiceDate;
    private Integer supplierRefId;
    private Date saleDate;
    private String sSaleDate;
    private String saleType;
    private String cNumberDisplay;
    private Integer cNumber;
    private Float coinage;
    private Float grossAmount;
    private Float taxAmount;
    private Float discountAmount;
    private String remarks;
    private String offVessal;
    private String lodingVessal;
    private Float plusAmount;
    private Float minusAmount;
    private Float amount;
    private Integer active;
    private Date created_Date;
    private String created_By;
    private Date modified_Date;
    private String modified_By;
    private Integer truckRefid;
    private Integer driverRefid;
    private Integer saleMasterRefId;
    private String jobNo;
    private Integer pStatus;
    private Float currencyValue;
    private Float actualAmount;
    private String description;
    private String billStatus;
    private String payTo;
    private Integer paymentTermsRefid;
    private Integer checkloadingVessel;
    private Integer checkoffgVessel;
    private Date dueDate;
    private String supplierName;
    private String sDueDate;
    private List<BillsOrderDetailsModel> billsOrderDetails;
}

