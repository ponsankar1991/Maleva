package my.maleva.api.module.rti.batch.dto;

import java.util.List;

/**
 * Wire types for "create every RTI in this plan in one click".
 *
 * <p>The whole feature turns on one promise: every job in the plan is
 * accounted for. A job is either inside a group that will become an RTI, or
 * inside {@code skipped} with a reason a planner can read. Nothing may fall
 * between the two — {@link PlanningRtiBatchPreview#isComplete()} is what
 * proves it, and the service refuses to answer when it does not hold.
 */
public final class PlanningRtiBatchDtos {

    private PlanningRtiBatchDtos() {
    }

    /** Why a planned job is not going to become part of an RTI right now. */
    public enum SkipReason {
        /** The job already sits in an active RTI — creating another would double it. */
        ALREADY_IN_RTI,
        /** No truck on the planning row, and no driver name to fall back on. */
        NO_TRUCK,
        /** The planning row carries no sale order reference (plan was never saved). */
        NO_JOB_REFERENCE,
        /** The same job is on the plan twice; it goes on one RTI line, not two. */
        DUPLICATE_IN_PLAN,
        /** The planner did not tick this job's truck, so no RTI was made for it. */
        NOT_CONFIRMED
    }

    /** Where a group's driver came from, so the planner knows what to trust. */
    public enum DriverSource {
        /** Typed or picked on the planning row itself. */
        PLAN,
        /** Nobody on the plan; taken from the last driver who ran this truck. */
        LAST_TRIP,
        /** An outside driver typed as free text on the planning row. */
        OUTSIDE,
        /** Still unknown — the planner has to choose before this group can be created. */
        NONE
    }
}
