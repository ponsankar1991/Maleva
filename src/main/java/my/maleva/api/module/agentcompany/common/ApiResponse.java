package my.maleva.api.module.agentcompany.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Standard API response wrapper for the entire application.
 * Use this for ALL controllers.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final int statusCode;
    private final String message;
    private final T data;
    private final Object meta;
    private final Object error;

    private ApiResponse(
            boolean success,
            HttpStatus status,
            String message,
            T data,
            Object meta,
            Object error
    ) {
        this.success = success;
        this.statusCode = status.value();
        this.message = message;
        this.data = data;
        this.meta = meta;
        this.error = error;
    }

    /* ================= SUCCESS ================= */

    public static <T> ApiResponse<T> success(
            String message,
            T data
    ) {
        return new ApiResponse<>(
                true,
                HttpStatus.OK,
                message,
                data,
                null,
                null
        );
    }

    public static <T> ApiResponse<T> success(
            String message,
            T data,
            Object meta
    ) {
        return new ApiResponse<>(
                true,
                HttpStatus.OK,
                message,
                data,
                meta,
                null
        );
    }

    /* ================= FAILURE ================= */

    public static <T> ApiResponse<T> failure(
            HttpStatus status,
            String message
    ) {
        return new ApiResponse<>(
                false,
                status,
                message,
                null,
                null,
                null
        );
    }

    public static <T> ApiResponse<T> failure(
            HttpStatus status,
            String message,
            Object error
    ) {
        return new ApiResponse<>(
                false,
                status,
                message,
                null,
                null,
                error
        );
    }

    /* ================= ERROR (Convenience Methods) ================= */

    /**
     * Convenience method for error responses with 400 Bad Request status
     */
    public static <T> ApiResponse<T> error(String message) {
        return failure(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Convenience method for error responses with custom status
     */
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return failure(status, message);
    }

    /**
     * Convenience method for error responses with error details
     */
    public static <T> ApiResponse<T> error(String message, Object errorDetails) {
        return failure(HttpStatus.BAD_REQUEST, message, errorDetails);
    }
}

