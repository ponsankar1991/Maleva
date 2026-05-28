package my.maleva.api.module.common.mapper;

import my.maleva.api.module.common.dto.WhatsAppMessageDto;
import org.mapstruct.Mapper;

/**
 * WhatsAppMessageMapper - MapStruct mapper for WhatsAppMessageDto
 * Handles conversion between WhatsAppMessageDto and service models
 */
@Mapper(componentModel = "spring")
public interface WhatsAppMessageMapper {

    /**
     * Convert WhatsAppMessageDto - identity mapping (no-op since source and target are the same)
     */
    default WhatsAppMessageDto toDto(WhatsAppMessageDto message) {
        return message;
    }

    /**
     * Create WhatsAppMessageDto from parameters
     */
    default WhatsAppMessageDto createTextMessage(String mobileData, String messageData) {
        return WhatsAppMessageDto.builder()
                .mobileData(mobileData)
                .messageData(messageData)
                .wType(1)  // Text type
                .build();
    }

    /**
     * Create WhatsAppMessageDto for image message
     */
    default WhatsAppMessageDto createImageMessage(String mobileData, String messageData, String imageUrl) {
        return WhatsAppMessageDto.builder()
                .mobileData(mobileData)
                .messageData(messageData)
                .urlData(imageUrl)
                .wType(2)  // Image type
                .build();
    }

    /**
     * Create WhatsAppMessageDto for PDF message
     */
    default WhatsAppMessageDto createPdfMessage(String mobileData, String messageData,
                                                String pdfUrl, String documentName) {
        return WhatsAppMessageDto.builder()
                .mobileData(mobileData)
                .messageData(messageData)
                .urlData(pdfUrl)
                .documentName(documentName)
                .wType(3)  // PDF type
                .build();
    }
}

