package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    private Integer userRefId;
    private Integer employeeRefId;

    @NotNull
    private Integer supplierRefId;

    @NotNull
    private LocalDateTime saleDate;

    @Size(max = 100)
    private String invoiceNo;

    @NotNull
    private LocalDateTime invoiceDate;

    @NotBlank
    @Size(max = 300)
    private String cNumberDisplay;

    @NotNull
    private Integer cNumber;

    @NotNull
    private Float coinage;

    @NotNull
    private Float grossAmount;

    @NotNull
    private Float taxAmount;

    @NotNull
    private Float discountAmount;

    @NotNull
    private Float plusAmount;

    @NotNull
    private Float minusAmount;

    @NotNull
    private Float amount;

    @Size(max = 300)
    private String remarks;

    @NotNull
    private Integer active;

    private LocalDateTime createdDate;

    @NotBlank
    @Size(max = 50)
    private String createdBy;

    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    private Integer truckRefid;
    private Integer driverRefid;

    @NotBlank
    @Size(max = 50)
    private String saleType;

    private Integer lastEmployeeRefId;
    private Integer saleMasterRefId;

    @NotNull
    private Integer paymentTermsRefid;

    private Integer pStatus;

    @Size(max = 200)
    private String description;

    @NotNull
    private Float currencyValue;

    @NotNull
    private Float actualAmount;

    @Size(max = 250)
    private String payTo;

    @Size(max = 250)
    private String billStatus;

    private LocalDate dueDate;
    private Integer fileupload;
}
