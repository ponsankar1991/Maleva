package my.maleva.api.module.planning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Planning Save operations.
 * Returns the saved planning ID and number for frontend reference.
 *
 * Matches .NET ResponseViewModel: { ok, message, Name (CNumberDisplay), Id }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningSaveResponseDto {

    /**
     * Success flag
     */
    private boolean ok;

    /**
     * Response message
     */
    private String message;

    /**
     * Planning number display (e.g., "PL000000001")
     */
    private String name;

    /**
     * Saved planning ID
     */
    private Integer id;

    /**
     * Helper to create success response.
     */
    public static PlanningSaveResponseDto success(String planingNo, Integer id) {
        return PlanningSaveResponseDto.builder()
                .ok(true)
                .message("Planning saved successfully")
                .name(planingNo)
                .id(id)
                .build();
    }

    /**
     * Helper to create error response.
     */
    public static PlanningSaveResponseDto error(String message) {
        return PlanningSaveResponseDto.builder()
                .ok(false)
                .message(message)
                .build();
    }
}
