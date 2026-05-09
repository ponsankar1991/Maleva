package my.maleva.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Firebase Message DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirebaseMessage {
    private String token;
    private FirebaseData data;
    private FirebaseNotification notification;
}
