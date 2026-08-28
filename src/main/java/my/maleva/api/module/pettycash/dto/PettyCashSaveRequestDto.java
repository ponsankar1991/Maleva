package my.maleva.api.module.pettycash.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * What the Petty Cash screen posts on Save.
 *
 * <p>{@code id} 0 or absent inserts; anything else updates that record.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashSaveRequestDto {

    private Integer id;

    private Integer employeeRefId;

    /** Legacy's "PayTo"/"Employee Type" combo value, stored as {@code Department}. */
    private String department;

    /** ISO {@code yyyy-MM-dd}; {@code dd/MM/yyyy} also accepted. */
    private String pettyCashDate;

    private String paymentStatus;

    private String remark;

    private List<PettyCashSaveLineDto> pettyCashDetails;
}
