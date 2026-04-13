package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for sales order status pivot data
 * Maps to legacy SelectSalesOrderStatus API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderStatusDto {

    @JsonProperty("statusList")
    private List<SalesOrderStatusItemDto> statusList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesOrderStatusItemDto {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("JobStatus")
        private String jobStatus;

        @JsonProperty("DayCount")
        private Integer dayCount;
    }
}
