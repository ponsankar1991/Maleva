package my.maleva.api.module.rti.batch.dto;

import java.util.List;

/**
 * One RTI-to-be: a truck, a driver and the jobs they run.
 *
 * <p>The grouping key is truck + driver + trip. {@code RTIMaster} holds a single
 * truck and a single driver, so those two are forced; the trip comes from the
 * planner's REMARKS ("1ST TRIP", "2ND TRIP"), because the same truck and driver
 * running a second load is a second run and belongs on its own RTI.
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
        /** "Trip 1", "Trip 2" from the planning REMARKS; empty when none was written. */
        String tripLabel,
        List<PlanningRtiJob> jobs,
        List<String> warnings) {

    /** True when this group has everything it needs to be saved as an RTI. */
    public boolean isReady() {
        return truckRefId != null && truckRefId > 0
                && driverRefId != null && driverRefId > 0
                && jobs != null && !jobs.isEmpty();
    }
}
