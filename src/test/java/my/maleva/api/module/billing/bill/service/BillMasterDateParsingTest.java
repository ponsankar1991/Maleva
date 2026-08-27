package my.maleva.api.module.billing.bill.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The bill search parses filter dates from the screen. Getting this wrong
 * silently returns the wrong month of bills rather than failing, so the
 * accepted formats are pinned here.
 */
class BillMasterDateParsingTest {

    @Test
    void parsesIsoDates() {
        assertThat(BillMasterTransactionService.parseDate("2026-08-27"))
                .isEqualTo(LocalDate.of(2026, 8, 27));
    }

    @Test
    void parsesIsoDateTimesFromDatePickers() {
        assertThat(BillMasterTransactionService.parseDate("2026-08-27T00:00:00"))
                .isEqualTo(LocalDate.of(2026, 8, 27));
    }

    @Test
    void parsesTheScreensDayFirstFormat() {
        assertThat(BillMasterTransactionService.parseDate("27/08/2026"))
                .isEqualTo(LocalDate.of(2026, 8, 27));
    }

    @Test
    void blankMeansNoBound() {
        assertThat(BillMasterTransactionService.parseDate(null)).isNull();
        assertThat(BillMasterTransactionService.parseDate("   ")).isNull();
    }

    @Test
    void refusesAmbiguousMonthFirstInputInsteadOfGuessing() {
        // 08/27/2026 is US-style; guessing day-first would silently read it as
        // an invalid day and quietly shift the whole filter window.
        assertThatThrownBy(() -> BillMasterTransactionService.parseDate("08/27/2026"))
                .isInstanceOf(RuntimeException.class);
    }
}
