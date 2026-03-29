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
public class PaymentVoucherMasterDto {
    private Integer id;

    private Integer companyRefId;
    private Integer userRefId;
    private Integer employeeRefId;
    private Integer lastEmployeeRefId;

    @NotNull
    private Integer cNumber;

    @NotNull
    @Size(max = 50)
    private String cNumberDisplay;

    @NotNull
    @Size(max = 200)
    private String payTo;

    @NotNull
    private Integer paymentById;

    @NotNull
    private LocalDateTime paymentVoucherDate;

    @NotNull
    @Size(max = 200)
    private String description;

    @NotNull
    @Size(max = 200)
    private String refNo;

    @NotNull
    private BigDecimal amount;

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

    @NotNull
    @Size(max = 50)
    private String paymentStatus;

    @NotNull
    private Float bankCharges;

    @NotNull
    private Float currencyValue;

    @NotNull
    private Float actualAmount;

    @Size(max = 50)
    private String qneCode;

    @Size(max = 50)
    private String qneId;

    private Integer billsOrderMasterRefId;
    private Integer paymentReceiptid;

    @Size(max = 100)
    private String eInvoiceUid;

    @Size(max = 100)
    private String eInvoiceSUid;

    @Size(max = 100)
    private String eInvoiceLongId;

    private LocalDateTime eInvoicePushDT;
    private LocalDateTime eInvoicePushVDT;

    @Size(max = 50)
    private String eInvoiceStatus;

    @Size(max = 500)
    private String payFrom;
}
