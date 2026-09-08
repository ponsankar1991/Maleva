package my.maleva.api.module.rti.batch.dto;

import java.util.List;

/**
 * The dry run: what one click would create, shown before anything is written.
 *
 * <p>Nothing in producing this touches the database for writing. The planner
 * reads it, fixes what is wrong, and only then confirms.
 *
 * @param plannedJobs jobs on the plan, counted once each
 * @param jobsToCreate jobs that will land on a new RTI
 * @param jobsSkipped  jobs that will not, each with a reason
 */
public record PlanningRtiBatchPreview(
        Integer planningId,
        String planningNo,
        String planningDate,
        int plannedJobs,
        int jobsToCreate,
        int jobsSkipped,
        List<PlanningRtiGroup> groups,
        List<PlanningRtiSkip> skipped,
        List<String> warnings) {

    /**
     * Every planned job is either going into an RTI or listed as skipped.
     *
     * <p>This is the arithmetic behind the promise that nothing is missed. The
     * service checks it before returning and refuses rather than hand back a
     * preview that quietly loses a job.
     */
    public boolean isComplete() {
        return plannedJobs == jobsToCreate + jobsSkipped;
    }

    /** Groups that still need a driver picked before they can be saved. */
    public long groupsNeedingDriver() {
        return groups == null ? 0 : groups.stream().filter(group -> !group.isReady()).count();
    }
}
