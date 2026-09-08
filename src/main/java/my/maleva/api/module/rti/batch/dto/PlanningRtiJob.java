package my.maleva.api.module.rti.batch.dto;

/**
 * One planned job as it will appear on an RTI line.
 *
 * <p>Everything here is read from the plan and its sale order; nothing is
 * invented. {@code salary} is deliberately absent — the driver's pay is
 * entered by a person on the RTI screen, never by this feature.
 *
 * <p>{@code existingRtiNo} is set when this job is already on an active RTI. It
 * only appears when the planner asked to include such jobs, and it is shown so a
 * second RTI is always a decision rather than an accident.
 */
public record PlanningRtiJob(
        Integer planningDetailId,
        Integer saleOrderMasterRefId,
        String jobNo,
        String customerName,
        String origin,
        String destination,
        String pickupDate,
        String deliveryDate,
        Integer sortBy,
        /** What the planner wrote in REMARKS - "1ST TRIP", "COMBINE", a note. */
        String remarks,
        Integer existingRtiId,
        String existingRtiNo,
        String existingRtiDate) {
}
