package my.maleva.api.module.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ErrorMessageDto - DTO for API error responses
 * Equivalent to .NET ErrorMsg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorMessageDto {

    private String code;

    private String message;
}

