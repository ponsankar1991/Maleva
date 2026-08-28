package my.maleva.api.module.transactionreport.service;

import my.maleva.api.module.transactionreport.dto.PaymentDoneRequestDto;
import my.maleva.api.module.transactionreport.dto.PaymentDoneViewDto;

/**
 * Reports over completed transactions.
 *
 * <p>The Java home of the legacy {@code TransactionReportServices}. Only the
 * Payment Completed report lives here so far; the rest of that class is still
 * served by the .NET host.
 */
public interface TransactionReportService {

    /**
     * Every payment settled in the requested window, from both registers.
     *
     * <p>Replaces legacy {@code POST /TransactionReport/SelectPaymentDone}.
     *
     * @param request filters; {@code comid} must already be resolved
     * @return the rows, their SQL-computed total, and the row count
     */
    PaymentDoneViewDto getCompletedPayments(PaymentDoneRequestDto request);
}
