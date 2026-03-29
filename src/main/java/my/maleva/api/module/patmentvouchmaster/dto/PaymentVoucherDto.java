package my.maleva.api.module.patmentvouchmaster.dto;

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
public class PaymentVoucherDto {
    private Integer id;

    private Integer companyRefId;
    private Integer userRefId;
    private Integer employeeRefId;
    private Integer lastEmployeeRefId;

    @NotNull
    private Integer cNumber;

    @NotNull
    @Size(max = 300)
    private String cNumberDisplay;

    @NotNull
    private Integer payToId;

    private Integer billToId;

    @NotNull
    private LocalDateTime paymentVoucherDate;

    @NotNull
    private Integer bankRefId;

    @Size(max = 100)
    private String refNumber;

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

    @Size(max = 3000)
    private String filePath;

    @NotNull
    private BigDecimal amount;

    @Size(max = 500)
    private String remarks;

    private LocalDateTime createdDate;

    @NotNull
    @Size(max = 200)
    private String createdBy;

    private LocalDateTime modifiedDate;

    @NotNull
    @Size(max = 200)
    private String modifiedBy;

    @NotNull
    private Integer active;

    private Integer paymentRefId;
    private Integer claimRefId;
    private Integer salaryRefId;
    private Integer expenseRefId;
    private Integer renewalRefId;

    @NotNull
    @Size(max = 50)
    private String payStatus;

    @NotNull
    private Integer payType;
}
