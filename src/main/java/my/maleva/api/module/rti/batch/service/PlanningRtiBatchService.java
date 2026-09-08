package my.maleva.api.module.rti.batch.service;

import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchPreview;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchRequest;
import my.maleva.api.module.rti.batch.dto.PlanningRtiBatchResult;

/** Creates every RTI a saved plan implies, in one click. */
public interface PlanningRtiBatchService {

    /**
     * What the plan would produce. Reads only — nothing is written, so a planner
     * can open this, look, and walk away with the database untouched.
     *
     * @param onlyJobIds when the planner ticked rows, the jobs those rows carry;
     *                   null or empty means the whole plan. Ticked or not, the
     *                   grouping is the same — one RTI per truck and driver —
     *                   and jobs outside the selection are still listed, so the
     *                   count of what is left uncovered stays honest.
     * @param includeExisting brings jobs that already sit on an active RTI back
     *                   into the preview, each marked with the RTI it is already
     *                   on. Creating from those makes a second RTI, which is
     *                   sometimes right (a job re-run on another truck) and is
     *                   always the planner's call, never a default.
     */
    PlanningRtiBatchPreview preview(Integer planningId, Integer companyRefId,
                                    java.util.List<Integer> onlyJobIds, boolean includeExisting);

    /**
     * Creates the RTIs the planner confirmed, in one transaction: either every
     * RTI in the batch exists afterwards, or none does.
     */
    PlanningRtiBatchResult create(Integer planningId, PlanningRtiBatchRequest request);
}
