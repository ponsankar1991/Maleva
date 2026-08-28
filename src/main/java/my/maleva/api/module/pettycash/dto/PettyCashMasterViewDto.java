package my.maleva.api.module.pettycash.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** One row of the petty cash F5 grid. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashMasterViewDto {

    private Integer id;

    private Integer cNumber;

    private Integer employeeRefId;

    private String cNumberDisplay;

    /** Petty cash date, {@code dd/MM/yyyy}. */
    private String sPettyCashDate;

    private String employeeName;

    private String department;

    private String remark;

    private String paymentStatus;

    /** Cast from the entity's String {@code Amount} column via {@code TRY_CAST}. */
    private BigDecimal amount;

    private Integer status;
}
