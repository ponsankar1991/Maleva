package my.maleva.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FireBaseRequestModel - DTO for Firebase notification requests
 * Equivalent to .NET FireBaseRequestModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FireBaseRequestModel {

    private String tokenid;
    private String message;
    private String title;
    private String topic;
    private String imageUrl;
    private String body;
    private String key_1;
    private String key_2;
}
