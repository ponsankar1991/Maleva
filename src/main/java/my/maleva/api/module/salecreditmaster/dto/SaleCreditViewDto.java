package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Everything the SALECREDIT ENTRY VIEW grid needs: the credit notes, their
 * product lines and their knock-off lines (the two expandable sub-grids), and
 * the total the window prints.
 *
 * <p>{@code totalAmount} is summed by the database over {@code numeric(18,2)};
 * legacy re-added the rows in JavaScript from float32 values.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditViewDto {

    private List<SaleCreditViewRowDto> creditNotes;

    private List<SaleCreditViewDetailDto> details;

    private List<SaleCreditViewKnockOffDto> knockOffs;

    private BigDecimal totalAmount;

    private int count;
}
