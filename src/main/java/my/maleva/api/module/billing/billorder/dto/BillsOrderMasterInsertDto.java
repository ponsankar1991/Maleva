package my.maleva.api.module.billing.billorder.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for inserting/updating BillsOrderMaster
 * Equivalent to .NET BillsOrderMasterModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderMasterInsertDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    private Integer sdId;

    @Min(value = 0, message = "Company ID must be positive")
    private Integer companyRefId;

    private Integer fileupload;

    private Integer userRefId;

    private Integer employeeRefId;

    @NotBlank(message = "Invoice No is required")
    @Size(max = 100, message = "Invoice No cannot exceed 100 characters")
    private String invoiceNo;

    @NotNull(message = "Invoice Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate invoiceDate;

    private String sInvoiceDate;

    @NotNull(message = "Supplier ID is required")
    @Min(value = 1, message = "Supplier ID must be positive")
    private Integer supplierRefId;

    @NotNull(message = "Sale Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate saleDate;

    private String sSaleDate;

    @NotBlank(message = "Sale Type is required")
    @Size(max = 50, message = "Sale Type cannot exceed 50 characters")
    private String saleType;

    private String cNumberDisplay;

    private Integer cNumber;

    @DecimalMax(value = "999999999.99", message = "Coinage amount is too large")
    private Float coinage;

    @NotNull(message = "Gross Amount is required")
    @DecimalMin(value = "0.0", message = "Gross Amount must be positive")
    private Float grossAmount;

    @NotNull(message = "Tax Amount is required")
    @DecimalMin(value = "0.0", message = "Tax Amount must be positive")
    private Float taxAmount;

    @DecimalMin(value = "0.0", message = "Discount Amount must be positive")
    private Float discountAmount;

    @Size(max = 300, message = "Remarks cannot exceed 300 characters")
    private String remarks;

    private String offVessal;

    private String lodingVessal;

    @DecimalMin(value = "0.0", message = "Plus Amount must be positive")
    private Float plusAmount;

    @DecimalMin(value = "0.0", message = "Minus Amount must be positive")
    private Float minusAmount;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be positive")
    private Float amount;

    private Integer active = 1;

    private LocalDateTime created_Date;

    private String created_By;

    private LocalDateTime modified_Date;

    private String modified_By;

    private Integer truckRefid;

    private Integer driverRefid;

    private Integer saleMasterRefId;

    @Size(max = 50, message = "Job No cannot exceed 50 characters")
    private String jobNo;

    private Integer pStatus;

    @DecimalMin(value = "0.0", message = "Currency Value must be positive")
    private Float currencyValue;

    @DecimalMin(value = "0.0", message = "Actual Amount must be positive")
    private Float actualAmount;

    @Size(max = 50, message = "Description cannot exceed 50 characters")
    private String description;

    @Size(max = 50, message = "Bill Status cannot exceed 50 characters")
    private String billStatus;

    @Size(max = 50, message = "Pay To cannot exceed 50 characters")
    private String payTo;

    private Integer paymentTermsRefid;

    private Integer checkloadingVessel;

    private Integer checkoffgVessel;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate dueDate;

    private String supplierName;

    private String sDueDate;

    @Valid
    @NotEmpty(message = "At least one bill order detail is required")
    private List<BillsOrderDetailsInsertDto> billsOrderDetails;
}

