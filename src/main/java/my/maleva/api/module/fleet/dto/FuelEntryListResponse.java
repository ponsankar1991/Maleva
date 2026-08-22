package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The fuel entry list plus the two totals the screen shows beside it.
 *
 * The legacy service stamped both totals onto every row of the grid, so the
 * same number was repeated thousands of times over the wire. They belong to the
 * result, not the row.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelEntryListResponse {

    private List<FuelEntryListItemDto> items;

    /** SUM(Amount) of PaymentVoucherMaster rows described as FUEL in the range. */
    private Double paymentVoucherTotal;

    /** SUM(Amount) of SubcdiyEntry rows in the range. */
    private Double subsidyTotal;

    /** SUM of AAmount over the returned rows - the legacy header figure. */
    private Double entriesTotal;
}
