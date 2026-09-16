package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * One day's truck count on the calendar: how many of our bookable trucks there
 * are, how many are taken and how many are still free.
 *
 * <p>Counted exactly as the availability endpoint counts, across every size: a
 * truck is taken when it holds at least one live OWN or SHARED order that day.
 * OUTSIDE orders use no truck of ours, so they are reported separately and never
 * reduce {@link #freeTrucks}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckDayCapacityDto {

    private LocalDate date;

    /** Orderable own trucks. */
    private Integer totalTrucks;

    private Integer takenTrucks;

    /** {@code totalTrucks - takenTrucks}; 0 means the day is full. */
    private Integer freeTrucks;

    /** Live OUTSIDE orders on the day. */
    private Integer outsideOrders;

    /** Trucks off the road that day (in the workshop), excluded from totalTrucks. */
    private Integer workshopTrucks;
}
