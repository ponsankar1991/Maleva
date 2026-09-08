package my.maleva.api.module.rti.batch.dto;

import java.util.List;

/**
 * What the confirm actually created.
 *
 * <p>Reported the same way as the preview so a planner can compare the two at a
 * glance: the counts must still add up, and anything that changed between
 * previewing and confirming (someone else created an RTI for a job in between)
 * shows up in {@code skipped} rather than silently disappearing.
 */
public record PlanningRtiBatchResult(
        Integer planningId,
        String planningNo,
        int plannedJobs,
        int jobsCreated,
        int jobsSkipped,
        List<Created> created,
        List<PlanningRtiSkip> skipped) {

    /** One RTI that now exists. */
    public record Created(
            Integer rtiId,
            String rtiNo,
            Integer truckRefId,
            String truckName,
            Integer driverRefId,
            String driverName,
            int jobCount) {
    }

    /** Same arithmetic promise as the preview, checked after the writes. */
    public boolean isComplete() {
        return plannedJobs == jobsCreated + jobsSkipped;
    }
}
