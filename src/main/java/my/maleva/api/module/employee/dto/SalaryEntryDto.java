package my.maleva.api.module.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * SalaryEntryDto
 * Data Transfer Object for SalaryEntry API layer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryEntryDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    private Integer lastEmployeeRefId;

    private Integer employeeRefId1;

    private Integer driverRefId;

    @NotNull(message = "Sale Date is required")
    private LocalDateTime saleDate;

    @NotBlank(message = "C Number Display is required")
    @Size(max = 300, message = "C Number Display cannot exceed 300 characters")
    private String cNumberDisplay;

    @NotNull(message = "C Number is required")
    private Integer cNumber;

    @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
    private String remarks;

    private Integer active;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount must be greater than or equal to 0")
    private Double amount;

    @Size(max = 3000, message = "File Path cannot exceed 3000 characters")
    private String filePath;

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
}

