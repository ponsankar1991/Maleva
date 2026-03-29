package my.maleva.api.module.salecreditmaster.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SaleCreditMasterDto
 * Data Transfer Object for SaleCreditMaster API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleCreditMasterDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    private Integer lastEmployeeRefId;

    @NotNull(message = "C Number is required")
    private Integer cNumber;

    @NotBlank(message = "C Number Display is required")
    @Size(max = 300, message = "C Number Display cannot exceed 300 characters")
    private String cNumberDisplay;

    @NotNull(message = "Customer Reference ID is required")
    private Integer customerRefId;

    @NotNull(message = "Sale Date is required")
    private LocalDateTime saleDate;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    private LocalDateTime createdDate;

    @Size(max = 200, message = "Created By cannot exceed 200 characters")
    private String createdBy;

    private LocalDateTime modifiedDate;

    @Size(max = 200, message = "Modified By cannot exceed 200 characters")
    private String modifiedBy;

    @Size(max = 100, message = "Reference Number cannot exceed 100 characters")
    private String refNumber;

    private Integer cStatus;

    @NotNull(message = "Sale Master Reference ID is required")
    private Integer saleMasterRefId;

    private Double currencyValue;

    private Double actualAmount;

    private Double coinage;

    private Double grossAmount;

    private Double taxAmount;

    private Double discountAmount;

    private Double plusAmount;

    private Double minusAmount;

    @Size(max = 50, message = "QNE Code cannot exceed 50 characters")
    private String qneCode;

    @Size(max = 50, message = "QNE Id cannot exceed 50 characters")
    private String qneId;

    @Size(max = 100, message = "E Invoice UID cannot exceed 100 characters")
    private String eInvoiceUid;

    @Size(max = 100, message = "E Invoice S UID cannot exceed 100 characters")
    private String eInvoiceSUid;

    @Size(max = 100, message = "E Invoice Long ID cannot exceed 100 characters")
    private String eInvoiceLongId;

    private LocalDateTime eInvoicePushDT;

    private LocalDateTime eInvoicePushVDT;

    @Size(max = 50, message = "E Invoice Status cannot exceed 50 characters")
    private String eInvoiceStatus;
}

