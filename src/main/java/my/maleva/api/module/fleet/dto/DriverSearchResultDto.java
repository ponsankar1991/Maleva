package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for driver search results with pagination support
 * Corresponds to C# SelectDriver response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverSearchResultDto {

    /**
     * List of driver records for current page
     */
    private List<DriverMasterDto> items;

    /**
     * Total count of records matching search criteria (including filters)
     * Used for pagination calculation
     */
    private Long totalCount;
}

