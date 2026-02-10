package my.maleva.api.common;

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
}

