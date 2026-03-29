package my.maleva.api.module.payment.dto;

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
public class PaymentDto {
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
    private Integer supplierRefId;

    @NotNull
    private LocalDateTime paymentDate;

    @NotNull
    private Integer bankRefId;

    @Size(max = 100)
    private String refNumber;

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

    @Size(max = 50)
    private String qneCode;

    @Size(max = 50)
    private String qneId;

    @NotNull
    @Size(max = 50)
    private String paymentStatus;

    @NotNull
    private Float bankCharges;

    @NotNull
    private Float currencyValue;

    @NotNull
    private Float actualAmount;

    @Size(max = 200)
    private String description;

    @Size(max = 100)
    private String payTo;
}
