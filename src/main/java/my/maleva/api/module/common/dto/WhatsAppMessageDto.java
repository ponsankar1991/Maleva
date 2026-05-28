package my.maleva.api.module.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * WhatsAppMessageDto - DTO for WhatsApp message parameters
 * Equivalent to .NET WhatsAppSendModel
 *
 * Fields:
 * - wType: 1=Text, 2=Image, 3=PDF
 * - mobileData: Comma-separated mobile numbers
 * - messageData: Message content
 * - urlData: Single URL (image or PDF)
 * - urlData1: Multiple URLs for bulk sends
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppMessageDto {

    @NotBlank(message = "Mobile data is required")
    private String mobileData;

    @NotBlank(message = "Message data is required")
    private String messageData;

    private String urlData;

    private List<String> urlData1;

    private String documentName;

    @NotNull(message = "Message type (wType) is required")
    private Integer wType;  // 1=Text, 2=Image, 3=PDF

    private String fromName;
}

