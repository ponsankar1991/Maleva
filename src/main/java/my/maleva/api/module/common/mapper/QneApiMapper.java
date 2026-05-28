package my.maleva.api.module.common.mapper;

import my.maleva.api.module.common.dto.QneApiRequestDto;
import org.mapstruct.Mapper;

/**
 * QneApiMapper - MapStruct mapper for QneApiRequestDto
 * Handles conversion for QNE API request models
 */
@Mapper(componentModel = "spring")
public interface QneApiMapper {

    /**
     * Convert QneApiRequestDto - identity mapping
     */
    QneApiRequestDto toDto(QneApiRequestDto request);

    /**
     * Create GET request
     */
    default QneApiRequestDto createGetRequest(String url) {
        return QneApiRequestDto.builder()
                .urlData(url)
                .type(1)  // GET
                .build();
    }

    /**
     * Create POST request
     */
    default QneApiRequestDto createPostRequest(String url, Object data) {
        return QneApiRequestDto.builder()
                .urlData(url)
                .data(data)
                .type(2)  // POST
                .build();
    }

    /**
     * Create PUT request
     */
    default QneApiRequestDto createPutRequest(String url, Object data) {
        return QneApiRequestDto.builder()
                .urlData(url)
                .data(data)
                .type(3)  // PUT
                .build();
    }
}

