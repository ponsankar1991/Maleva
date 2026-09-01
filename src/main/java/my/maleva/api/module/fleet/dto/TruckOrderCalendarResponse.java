package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The orders in the selected range, plus the two counters the legacy summary
 * row showed under the calendar.
 *
 * The counts are computed here rather than in the browser so the footer cannot
 * disagree with the grid - the legacy page derived them from whatever happened
 * to be in its {@code allOrders} array.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckOrderCalendarResponse {

    private List<TruckOrderDto> items;

    /** Legacy {@code lblTotalCount}: how many orders fall in the range. */
    private Integer totalOrders;

    /** Legacy {@code lblBookedCount}: how many distinct trucks are booked in it. */
    private Integer bookedTrucks;
}
