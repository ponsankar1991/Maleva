package my.maleva.api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TruckComboRequest - Production-Grade Request DTO for Truck Combo Queries
 * 
 * Used with POST /api/v1/truck-combo endpoint
 * Includes comprehensive input validation annotations
 * 
 * Validation Rules:
 * - companyId: Required, must be positive integer
 * - type: Optional, if provided must be 1-50 characters
 * 
 * Example:
 * {
 *   "companyId": 1,
 *   "type": "40FT"
 * }
 * 
 * @since 2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckComboRequest {

    /**
     * Company ID - Required
     * Must be a positive integer
     */
    @NotNull(message = "Company ID is required and cannot be null")
    @Positive(message = "Company ID must be a positive integer")
    private Integer companyId;

    /**
     * Truck Type Filter - Optional
     * If provided, must be between 1-50 characters
     * Examples: "40FT", "20FT", "CONTAINER", etc.
     */
    @Size(min = 1, max = 50, message = "Truck type must be between 1 and 50 characters")
    private String type;
}

