package my.maleva.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Firebase Root DTO for sending messages
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirebaseRoot {
    private FirebaseMessage message;
}
