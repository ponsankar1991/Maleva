package my.maleva.api.module.billing.bill.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bill numbers are a legacy contract: BIL + 2-digit year + 2-digit month + /
 * + a 3-digit counter that restarts every month (BIL2508/001). Existing bills
 * carry this shape, so the format cannot drift.
 */
class BillNumberFormatTest {

    @Test
    void formatsLikeTheLegacySequenceQuery() {
        assertThat(BillMasterTransactionService.formatBillNumber(LocalDate.of(2026, 8, 27), 1))
                .isEqualTo("BIL2608/001");
    }

    @Test
    void padsMonthAndCounter() {
        assertThat(BillMasterTransactionService.formatBillNumber(LocalDate.of(2026, 1, 5), 7))
                .isEqualTo("BIL2601/007");
    }

    @Test
    void countersPastNineHundredNinetyNineKeepTheirDigits() {
        // The counter is monthly, so four digits is an overflow month rather
        // than an error — the number must stay unique, not stay 3 digits.
        assertThat(BillMasterTransactionService.formatBillNumber(LocalDate.of(2026, 12, 31), 1234))
                .isEqualTo("BIL2612/1234");
    }
}
