package my.maleva.api.module.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One customer's billed sale orders in a date window — a row of the
 * "Completed" panel on the Accounts Receivable desk.
 *
 * <p>Port of the legacy {@code DashBoardServices.CompletedPaymentDB} row set.
 * Despite the name, that query has nothing to do with payments: it is sale
 * orders that already carry an invoice ({@code InvoiceNo <> 0}), grouped by
 * customer. The name is not carried over.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivableBilledCustomerDto {

    private Integer customerRefId;

    private String customerName;

    /** How many billed sale orders make up the total. */
    private Integer jobCount;

    /** The sum of those orders' amounts, rounded to the sen. */
    private Double netAmount;
}
