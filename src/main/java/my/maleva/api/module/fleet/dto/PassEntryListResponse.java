package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The levi entry list with its footer total.
 *
 * The legacy grid summed {@code Amount} in the browser after loading every row.
 * The total is computed server-side so it stays right regardless of paging or
 * client rounding.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassEntryListResponse {

    private List<PassEntryListItemDto> items;

    /** Sum of {@code amount} across {@link #items}. */
    private Double entriesTotal;
}
