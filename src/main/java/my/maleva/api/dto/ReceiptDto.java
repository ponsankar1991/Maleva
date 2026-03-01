package my.maleva.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

/**
 * ReceiptDto
 * Data Transfer Object for Receipt API layer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptDto {

    private Integer id;

    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    private Integer lastEmployeeRefId;

    @NotNull(message = "CNumber is required")
    private Integer cNumber;

    @NotBlank(message = "CNumber Display is required")
    @Size(max = 300, message = "CNumber Display cannot exceed 300 characters")
    private String CNumberDisplay;

    @NotNull(message = "Customer Reference ID is required")
    private Integer customerRefId;

    @NotNull(message = "Receipt Date is required")
    private LocalDateTime receiptDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be 0 or greater")
    private BigDecimal amount;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    private LocalDateTime createdDate;

    private String createdBy;

    private LocalDateTime modifiedDate;

    private String modifiedBy;

    @NotNull(message = "Bank Reference ID is required")
    private Integer bankRefId;

    @Size(max = 100, message = "Reference Number cannot exceed 100 characters")
    private String refNumber;

    private Integer pvStatus;

    @Size(max = 100, message = "TIN No cannot exceed 100 characters")
    private String tinNo;

    @Size(max = 100, message = "SST No cannot exceed 100 characters")
    private String sstNo;

    @Size(max = 100, message = "MSIC Code cannot exceed 100 characters")
    private String msicCode;

    @Size(max = 100, message = "Service Tax Type cannot exceed 100 characters")
    private String serviceTaxType;

    @Size(max = 100, message = "Bank Name cannot exceed 100 characters")
    private String bankName;

    @Size(max = 100, message = "Account No cannot exceed 100 characters")
    private String accountNo;

    @Size(max = 50, message = "QNE Code cannot exceed 50 characters")
    private String qneCode;

    @Size(max = 50, message = "QNE Id cannot exceed 50 characters")
    private String qneId;

    @Min(value = 0, message = "Currency Value must be 0 or greater")
    private Double currencyValue;

    @Min(value = 0, message = "Actual Net Amount must be 0 or greater")
    private Double actualNetAmount;

    @Min(value = 0, message = "Bank Charges must be 0 or greater")
    private Double bankCharges;

    private Integer name;

    private Integer fileUpload;

    private List<ReceiptDetailsDto> receiptDetails;
}

