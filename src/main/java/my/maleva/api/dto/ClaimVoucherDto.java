package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimVoucherDto {
    private Integer id;

    private Integer companyRefId;
    private Integer userRefId;
    private Integer employeeRefId;
    private Integer lastEmployeeRefId;

    @NotNull
    private Integer cNumber;

    @NotBlank
    @Size(max = 300)
    private String cNumberDisplay;

    @NotNull
    private Integer supplierRefId;

    private Integer supplierRefId1;

    @NotNull
    private LocalDateTime claimVoucherDate;

    @NotNull
    private Integer bankRefId;

    @Size(max = 100)
    private String refNumber;

    @Size(max = 3000)
    private String filePath;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal amount;

    @Size(max = 500)
    private String remarks;

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

    private LocalDateTime createdDate;

    @NotBlank
    @Size(max = 200)
    private String createdBy;

    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 200)
    private String modifiedBy;

    @NotNull
    private Integer active;

    private Integer pvStatus;
}
