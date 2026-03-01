package my.maleva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseMasterDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    @NotNull(message = "Supplier Reference ID is required")
    private Integer supplierRefId;

    @NotNull(message = "Sale Date is required")
    private LocalDate saleDate;

    @Size(max = 100, message = "Invoice No cannot exceed 100 characters")
    private String invoiceNo;

    @NotNull(message = "Invoice Date is required")
    private LocalDate invoiceDate;

    @NotBlank(message = "CNumber Display is required")
    @Size(max = 300, message = "CNumber Display cannot exceed 300 characters")
    private String cNumberDisplay;

    @NotNull(message = "CNumber is required")
    private Integer cNumber;

    @Min(value = 0, message = "Coinage must be 0 or greater")
    private Double coinage;

    @Min(value = 0, message = "Gross Amount must be 0 or greater")
    private Double grossAmount;

    @Min(value = 0, message = "Tax Amount must be 0 or greater")
    private Double taxAmount;

    @Min(value = 0, message = "Discount Amount must be 0 or greater")
    private Double discountAmount;

    @Min(value = 0, message = "Plus Amount must be 0 or greater")
    private Double plusAmount;

    @Min(value = 0, message = "Minus Amount must be 0 or greater")
    private Double minusAmount;

    @Min(value = 0, message = "Amount must be 0 or greater")
    private Double amount;

    @Size(max = 300, message = "Remarks cannot exceed 300 characters")
    private String remarks;

    private Integer active;

    private LocalDateTime createdDate;

    private String createdBy;

    private LocalDateTime modifiedDate;

    private String modifiedBy;

    private Integer truckRefId;

    private Integer driverRefId;

    @NotBlank(message = "Sale Type is required")
    @Size(max = 50, message = "Sale Type cannot exceed 50 characters")
    private String saleType;

    private Integer lastEmployeeRefId;

    private Integer purchaseOrderMasterRefId;

    @NotNull(message = "Payment Terms Reference ID is required")
    private Integer paymentTermsRefId;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    @Min(value = 0, message = "Currency Value must be 0 or greater")
    private Double currencyValue;

    @Min(value = 0, message = "Actual Amount must be 0 or greater")
    private Double actualAmount;

    @Size(max = 100, message = "Serial No cannot exceed 100 characters")
    private String serialNo;

    private String buyEmployeeName;

    private List<PurchaseDetailsDto> purchaseDetails;
}

