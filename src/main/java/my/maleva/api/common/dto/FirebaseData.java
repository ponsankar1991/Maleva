package my.maleva.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Firebase Data DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirebaseData {
    private String title;
    private String body;
    private String key_1;
    private String key_2;
}
