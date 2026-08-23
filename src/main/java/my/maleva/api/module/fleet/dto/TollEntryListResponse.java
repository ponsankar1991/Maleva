package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The toll entry list plus its header total.
 *
 * The legacy SelectTollEntry returned the master rows and every detail row of
 * every master in one payload, so opening a month pulled thousands of
 * transactions the grid never showed. Details are fetched per entry now.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollEntryListResponse {

    private List<TollEntryListItemDto> items;

    /** Sum of Amount over the returned rows. */
    private Double entriesTotal;
}
