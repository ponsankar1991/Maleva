package my.maleva.api.module.rti.batch.dto;

import java.util.List;

/**
 * One RTI-to-be: a truck, a driver and the jobs they run.
 *
 * <p>The grouping key is truck + pickup day, because {@code RTIMaster} holds a
 * single truck and a single driver, and because the history says a truck keeps
 * one driver through the day in 97% of cases. When a plan does put two drivers
 * on one truck in one day, the group splits — the schema leaves no choice.
 *
 * @param groupKey     stable identity of this group, echoed back on confirm
 * @param driverSource where the driver came from; {@code NONE} means the
 *                     planner must choose one before this group can be created
 * @param warnings     things worth reading before confirming — an expired
 *                     licence, a driver on leave, a suggested driver
 */
public record PlanningRtiGroup(
        String groupKey,
        Integer truckRefId,
        String truckName,
        Integer driverRefId,
        String driverName,
        PlanningRtiBatchDtos.DriverSource driverSource,
        String outsideDriver,
        String pickupDate,
        List<PlanningRtiJob> jobs,
        List<String> warnings) {

    /** True when this group has everything it needs to be saved as an RTI. */
    public boolean isReady() {
        return truckRefId != null && truckRefId > 0
                && driverRefId != null && driverRefId > 0
                && jobs != null && !jobs.isEmpty();
    }
}
