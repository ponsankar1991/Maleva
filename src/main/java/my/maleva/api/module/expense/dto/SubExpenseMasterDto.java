package my.maleva.api.module.expense.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SubExpenseMasterDto - DTO for SubExpenseMaster
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubExpenseMasterDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    @NotNull(message = "Expense Master Reference ID is required")
    private Integer expenseMasterRefId;

    @NotBlank(message = "Description is required")
    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    @NotNull(message = "Due Amount is required")
    private Float dueAmount;

    @NotNull(message = "Active status is required")
    private Integer active;

    private LocalDateTime dueFromDate;

    private LocalDateTime dueToDate;

    private Integer dueDate;

    @NotNull(message = "Account Reference ID is required")
    private Integer accountRefid;

    @Size(max = 100, message = "TIN No must not exceed 100 characters")
    private String tinNo;

    @Size(max = 100, message = "SST No must not exceed 100 characters")
    private String sstNo;

    @Size(max = 100, message = "MSIC Code must not exceed 100 characters")
    private String msicCode;

    @Size(max = 100, message = "Service Tax Type must not exceed 100 characters")
    private String serviceTaxType;

    @Size(max = 100, message = "Bank Name must not exceed 100 characters")
    private String bankName;

    @Size(max = 100, message = "Account No must not exceed 100 characters")
    private String accountNo;

    private Integer glAccountRefId;
}

