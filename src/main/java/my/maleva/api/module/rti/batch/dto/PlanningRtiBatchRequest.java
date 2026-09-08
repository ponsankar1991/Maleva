package my.maleva.api.module.rti.batch.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * The confirm step: create these RTIs from this plan.
 *
 * <p>The groups are posted back rather than recomputed from scratch so that a
 * driver the planner corrected in the preview is the driver that gets saved.
 * The server still re-reads the plan and re-checks for existing RTIs inside the
 * transaction — the client decides who drives, never what may be created.
 */
@Data
public class PlanningRtiBatchRequest {

    @NotNull
    private Integer companyRefId;

    private Integer employeeRefId;

    private Integer userRefId;

    /** Groups exactly as previewed, with any driver corrections applied. */
    @NotNull
    private List<Group> groups;

    /**
     * Allows a second RTI for a job that already has one.
     *
     * <p>Off by default, and the server enforces that: without it, a job already
     * on an active RTI is skipped however it was posted. A planner turns it on
     * in the preview, having seen which RTI each job is already on — doubling a
     * job's paperwork is a decision, never a side effect of pressing a button
     * twice.
     */
    private boolean allowDuplicates;

    /** One RTI to create. */
    @Data
    public static class Group {

        private String groupKey;

        @NotNull
        private Integer truckRefId;

        @NotNull
        private Integer driverRefId;

        /** Free-text name when the driver is an outside driver. */
        private String outsideDriver;

        private String outsideTruck;

        /** The jobs on this RTI. Must all belong to the plan being created from. */
        @NotNull
        private List<Integer> saleOrderMasterRefIds;
    }
}
