package my.maleva.api.module.pettycash.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * The petty cash F5 grid: matching records plus every line beneath them, which
 * the grid nests by {@code pettyCashMasterRefId}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashF5ViewDto {

    private List<PettyCashMasterViewDto> pettyCashMaster;

    private List<PettyCashDetailViewDto> pettyCashDetails;

    private BigDecimal totalAmount;

    private Integer count;
}
