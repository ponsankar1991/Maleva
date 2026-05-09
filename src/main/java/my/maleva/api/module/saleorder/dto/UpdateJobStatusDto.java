package my.maleva.api.module.saleorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateJobStatusDto
 *
 * DTO for updating Sale Order Job Status
 * Equivalent C# Method: public ResponseViewModel UpdateJobStatus(Int32 Id, Int32 JobStatusId)
 *
 * Request body for:
 * PUT /api/sale-orders/{id}/job-status
 * POST /api/sale-orders/update-job-status
 *
 * Example:
 * {
 *   "jobStatusId": 1
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJobStatusDto {

    /**
     * New Job Status ID
     * Represents the new status value to be set on JStatus field
     */
    @NotNull(message = "Job Status ID is required")
    @Positive(message = "Job Status ID must be a positive number")
    private Integer jobStatusId;
}

