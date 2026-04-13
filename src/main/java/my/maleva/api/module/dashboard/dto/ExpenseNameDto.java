package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for expense name data
 * Maps to legacy SelectExpenseName API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseNameDto {

    @JsonProperty("expenses")
    private List<ExpenseNameItemDto> expenses;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseNameItemDto {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("ExpCount")
        private Integer expCount;

        @JsonProperty("ExpAmount")
        private Double expAmount;

        @JsonProperty("ExpenseName")
        private String expenseName;
    }
}
