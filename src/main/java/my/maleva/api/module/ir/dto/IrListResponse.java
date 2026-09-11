package my.maleva.api.module.ir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The list plus its total.
 *
 * <p>The total is summed here rather than in the browser: a grid that adds up
 * its own rows drifts from the database as soon as paging or a hidden row is
 * introduced.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrListResponse {

    private List<IrDetailDto> items;

    private int count;

    /** Sum of ActualAmount over the filtered rows. */
    private long totalAmount;
}
