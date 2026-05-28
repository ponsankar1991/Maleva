package my.maleva.api.module.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * QneApiRequestDto - DTO for QNE API requests
 * Equivalent to .NET QneSendModel
 *
 * Request Type Constants:
 * - 1: GET request
 * - 2: POST request
 * - 3: PUT request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QneApiRequestDto {

    @NotBlank(message = "QNE API URL is required")
    private String urlData;

    private Object data;

    @NotNull(message = "Request type is required")
    private Integer type;  // 1=GET, 2=POST, 3=PUT

    public boolean isGetRequest() {
        return type != null && type == 1;
    }

    public boolean isPostRequest() {
        return type != null && type == 2;
    }

    public boolean isPutRequest() {
        return type != null && type == 3;
    }
}

