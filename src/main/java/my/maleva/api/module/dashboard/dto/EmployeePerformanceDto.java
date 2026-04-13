package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for employee performance (pivot table)
 * Maps to legacy SelectSaleorderdetailbasedonEmployee API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePerformanceDto {

    @JsonProperty("performance")
    private List<EmployeePerformanceItemDto> performance;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeePerformanceItemDto {
        @JsonProperty("EmployeeName")
        private String employeeName;

        @JsonProperty("Assigned")
        private Integer assigned;

        @JsonProperty("WaitingForBilling")
        private Integer waitingForBilling;

        @JsonProperty("DeliveryDone")
        private Integer deliveryDone;

        @JsonProperty("AtWarehouse")
        private Integer atWarehouse;

        @JsonProperty("TBA")
        private Integer tba;

        @JsonProperty("ZCancel")
        private Integer zCancel;

        @JsonProperty("Enquiry")
        private Integer enquiry;
    }
}
