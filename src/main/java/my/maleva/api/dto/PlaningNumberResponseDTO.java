package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for PLANNING sequence number generation
 * Uses MapStruct for entity-DTO conversion
 *
 * This DTO is used as an alternative response format for the PLANNING sequence endpoints
 * Provides a cleaner, typed response structure compared to raw Map<String, Object>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaningNumberResponseDTO {

    /**
     * The generated PLANNING sequence number (e.g., "PL000000001")
     */
    private String sequenceNumber;

    /**
     * The company ID
     */
    private Integer companyId;

    /**
     * Success flag
     */
    private Boolean success;

    /**
     * Error message (populated only on failure)
     */
    private String error;
}

