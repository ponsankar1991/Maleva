package my.maleva.api.module.paymentrecept.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * The RECEIPT ENTRY VIEW grid: receipts, the documents each one settles, and
 * the exact total — the port of legacy {@code ReceiptF5view}.
 *
 * <p>{@code totalAmount} is summed by the database over {@code numeric(18,2)};
 * the screen prints it and never re-adds the rows.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptViewDto {
    private List<ReceiptViewRowDto> receiptMaster;
    private List<ReceiptViewDetailDto> receiptDetails;
    private BigDecimal totalAmount;
    private int count;
}
