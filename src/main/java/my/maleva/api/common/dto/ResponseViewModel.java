package my.maleva.api.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ResponseViewModel - Generic response wrapper for all API endpoints
 * Equivalent to .NET ResponseViewModel
 *
 * Structure:
 * {
 *   "isSuccess": true/false,
 *   "statusCode": 200/400/500,
 *   "message": "Success message or error message",
 *   "data1": {...}
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseViewModel {

    @JsonProperty("isSuccess")
    private boolean isSuccess;

    @JsonProperty("statusCode")
    private Integer statusCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data1")
    private Object data1;

    @JsonProperty("data2")
    private Object data2;

    @JsonProperty("data3")
    private Object data3;
    
    // Explicitly add isSuccess() to handle Lombok edge cases with boolean/Boolean fields
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Builder for success response
     */
    public static ResponseViewModel success(Object data, String message, int statusCode) {
        return ResponseViewModel.builder()
                .isSuccess(true)
                .statusCode(statusCode)
                .message(message != null ? message : "Success")
                .data1(data)
                .build();
    }

    /**
     * Builder for error response
     */
    public static ResponseViewModel error(String message, int statusCode) {
        return ResponseViewModel.builder()
                .isSuccess(false)
                .statusCode(statusCode)
                .message(message)
                .build();
    }
}
