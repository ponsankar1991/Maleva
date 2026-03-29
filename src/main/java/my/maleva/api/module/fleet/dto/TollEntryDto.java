package my.maleva.api.module.fleet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TollEntryDto - DTO for TollEntry
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollEntryDto {

    private Integer id;

    @NotNull(message = "Company Reference ID is required")
    private Integer companyRefId;

    private Integer userRefId;

    private Integer employeeRefId;

    private Integer lastEmployeeRefId;

    private Integer truckRefid;

    @NotNull(message = "Sale Date is required")
    private LocalDateTime saleDate;

    @NotBlank(message = "C Number Display is required")
    @Size(max = 300, message = "C Number Display must not exceed 300 characters")
    private String cNumberDisplay;

    @NotNull(message = "C Number is required")
    private Integer cNumber;

    @Size(max = 2000, message = "Remarks must not exceed 2000 characters")
    private String remarks;

    @NotNull(message = "Active status is required")
    private Integer active;

    @NotNull(message = "Amount is required")
    private Float amount;

    @Size(max = 3000, message = "File Path must not exceed 3000 characters")
    private String filePath;

    private LocalDateTime createdDate;

    private String createdBy;

    private LocalDateTime modifiedDate;

    private String modifiedBy;
}

