package my.maleva.api.module.purchase.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Response DTO for SelectSparePartsView operation
 * Wraps the list of spare parts report data with status information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectSparePartsViewResponseDto {

    private boolean ok;

    private String message;

    private List<SparePartsReportViewDto> data;
}

