package my.maleva.api.module.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * QneFileDto - DTO for QNE file response
 * Equivalent to .NET QNEURLModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QneFileDto {

    private String file;
}

