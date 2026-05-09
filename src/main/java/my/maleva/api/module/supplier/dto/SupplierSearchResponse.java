package my.maleva.api.module.supplier.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SupplierSearchResponse - DTO for paginated supplier search results
 * Contains list of suppliers and total count for pagination
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierSearchResponse {
    private boolean ok;
    private String message;
    private List<SupplierDto> data;
    private Integer count;
    private Integer totalPages;
    private Integer currentPage;
}

