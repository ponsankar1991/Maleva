package my.maleva.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Firebase Notification DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirebaseNotification {
    private String title;
    private String body;
    private String image;
}
