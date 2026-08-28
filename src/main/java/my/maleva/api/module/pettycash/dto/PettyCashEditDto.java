package my.maleva.api.module.pettycash.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** One petty cash record, loaded back into the screen. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashEditDto {

    private Integer id;

    private Integer companyRefId;

    private Integer employeeRefId;

    private Integer cNumber;

    private String cNumberDisplay;

    private String department;

    private LocalDateTime pettyCashDate;

    /** {@code dd/MM/yyyy}, for the date picker. */
    private String sPettyCashDate;

    private String paymentStatus;

    private String remark;

    /** Matches the entity's String column — not converted to BigDecimal here. */
    private String amount;

    private List<PettyCashEditLineDto> pettyCashDetails;
}
