package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseEntryDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    private Integer userRefId;
    private Integer employeeRefId;

    @NotNull
    private Integer expenseMasterRefId;

    @NotNull
    private Integer subExpenseMasterRefId;

    @NotNull
    private LocalDateTime saleDate;

    @NotBlank
    @Size(max = 300)
    private String cNumberDisplay;

    @NotNull
    private Integer cNumber;

    @Size(max = 2000)
    private String remarks;

    @NotNull
    private Integer active;

    @NotNull
    private Float amount;

    @Size(max = 3000)
    private String filePath;

    private LocalDateTime createdDate;

    @NotBlank
    @Size(max = 50)
    private String createdBy;

    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    private Integer lastEmployeeRefId;

    @NotNull
    private Integer bankRefId;

    @Size(max = 100)
    private String refNumber;

    private Integer pvStatus;

    @Size(max = 100)
    private String tinNo;

    @Size(max = 100)
    private String sstNo;

    @Size(max = 100)
    private String msicCode;

    @Size(max = 100)
    private String serviceTaxType;

    @Size(max = 100)
    private String bankName;

    @Size(max = 100)
    private String accountNo;
}
