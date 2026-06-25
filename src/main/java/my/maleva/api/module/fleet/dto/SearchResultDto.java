package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SearchResultDto - contains search results and total count
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResultDto {
    private List<TruckMasterDto> items;
    private long totalCount;
}

