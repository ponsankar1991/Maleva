package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for pending payment data response
 * Maps to legacy SelectPendingPaymentDB API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingPaymentDto {

    @JsonProperty("pendingPayments")
    private List<PendingPaymentItemDto> pendingPayments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingPaymentItemDto {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("ExpenseName")
        private String expenseName;

        @JsonProperty("SubExpenseName")
        private String subExpenseName;

        @JsonProperty("Amount")
        private Double amount;

        @JsonProperty("DueDate")
        private String dueDate;

        @JsonProperty("DueReportId")
        private Integer dueReportId; // 0=NotDue, 1=Expiring, 2=Expired

        @JsonProperty("DetailedId")
        private Integer detailedId; // 0=SubExpense, 1=Vendor

        @JsonProperty("BankName")
        private String bankName;

        @JsonProperty("AccountNo")
        private String accountNo;
    }

    // Separate DTO for unreleased forwarding numbers
    @JsonProperty("unreleasedNumbers")
    private List<UnreleasedNumberDto> unreleasedNumbers;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnreleasedNumberDto {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("BillNoDisplay")
        private String billNoDisplay;

        @JsonProperty("DayCount")
        private Integer dayCount;

        @JsonProperty("Remarks")
        private String remarks;
    }

    // Completed payment DTO
    @JsonProperty("completedPayments")
    private List<CompletedPaymentDto> completedPayments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedPaymentDto {
        @JsonProperty("Id")
        private Integer id;

        @JsonProperty("CustomerName")
        private String customerName;

        @JsonProperty("NetAmt")
        private Double netAmt;
    }
}
