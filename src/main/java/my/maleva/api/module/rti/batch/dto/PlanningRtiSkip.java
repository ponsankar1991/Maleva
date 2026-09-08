package my.maleva.api.module.rti.batch.dto;

/**
 * A planned job that will not become part of an RTI, and why.
 *
 * <p>This list is the half of the preview a planner actually needs to read.
 * Every job left out appears here — silence is never allowed to mean "handled".
 */
public record PlanningRtiSkip(
        Integer planningDetailId,
        Integer saleOrderMasterRefId,
        String jobNo,
        String customerName,
        String truckName,
        PlanningRtiBatchDtos.SkipReason reason,
        String message,
        Integer existingRtiId,
        String existingRtiNo) {
}
