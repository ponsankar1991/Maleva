package my.maleva.api.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApiResponse - Generic response wrapper for API responses
 * Equivalent to the .NET ResponseViewModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    @JsonProperty("IsSuccess")
    private Boolean isSuccess;

    @JsonProperty("StatusCode")
    private Integer statusCode;

    @JsonProperty("Message")
    private String message;

    @JsonProperty("Data1")
    private T data1;
    @JsonProperty("Data")
    private T data;

    @JsonProperty("Data2")
    private Object data2;

    @JsonProperty("Data3")
    private String data3;

    @JsonProperty("Data4")
    private Object data4;

    @JsonProperty("ErrorDetails")
    private String errorDetails;
    
    // Explicitly add isSuccess() to handle Lombok edge cases with Boolean fields
    public Boolean isSuccess() {
        return isSuccess;
    }

    // Explicitly add getStatusCode() for the same reason
    public Integer getStatusCode() {
        return statusCode;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .isSuccess(true)
                .statusCode(200)
                .message(message)
                .data1(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Integer statusCode) {
        return ApiResponse.<T>builder()
                .isSuccess(false)
                .statusCode(statusCode)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Integer statusCode, String errorDetails) {
        return ApiResponse.<T>builder()
                .isSuccess(false)
                .statusCode(statusCode)
                .message(message)
                .errorDetails(errorDetails)
                .build();
    }
}
