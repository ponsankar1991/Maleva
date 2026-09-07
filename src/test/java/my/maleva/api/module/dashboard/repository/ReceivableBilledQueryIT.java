package my.maleva.api.module.dashboard.repository;

import my.maleva.api.module.dashboard.dto.ReceivableBilledCustomerDto;
import my.maleva.api.module.dashboard.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the Accounts Receivable desk's "Billed this month" query against
 * MalevanewDemo.
 *
 * <p>It is hand-written SQL, so a wrong column name or a bad GROUP BY only
 * fails when the endpoint is actually called — no unit test and no successful
 * startup would catch it. The assertions do not depend on the data: a window
 * far in the past is empty, and the current month is checked for shape rather
 * than for particular customers.
 */
@SpringBootTest
@Transactional
class ReceivableBilledQueryIT {

    private static final int COMPANY = 6;

    @Autowired private DashboardRepository repository;
    @Autowired private DashboardService service;

    @Test
    void theQueryExecutesAndAnEmptyWindowIsEmpty() {
        assertThat(repository.getReceivableBilledByCustomer(COMPANY, "1990-01-01", "1990-01-31")).isEmpty();
    }

    @Test
    void everyRowIsANamedCustomerWithACountAndATotal() {
        List<ReceivableBilledCustomerDto> rows =
                service.getReceivableBilledByCustomer(COMPANY, "2026-01-01", "2026-12-31");

        // Whether or not this company billed anything, the shape must hold —
        // an all-null row is what a BeanPropertyRowMapper would have produced,
        // and it reads on screen as a quiet day rather than a fault.
        assertThat(rows).allSatisfy(row -> {
            assertThat(row.getCustomerRefId()).isNotNull();
            assertThat(row.getCustomerName()).isNotNull();
            assertThat(row.getJobCount()).isNotNull().isPositive();
            assertThat(row.getNetAmount()).isNotNull();
        });
    }

    @Test
    void rowsComeBackLargestFirst() {
        List<Double> amounts = service.getReceivableBilledByCustomer(COMPANY, "2026-01-01", "2026-12-31")
                .stream().map(ReceivableBilledCustomerDto::getNetAmount).toList();

        // Legacy ordered by a DayCount column it never selected, so the order
        // was whatever the engine returned.
        assertThat(amounts).isSortedAccordingTo((left, right) -> Double.compare(right, left));
    }

    @Test
    void oneCustomerAppearsOnce() {
        List<ReceivableBilledCustomerDto> rows =
                service.getReceivableBilledByCustomer(COMPANY, "2026-01-01", "2026-12-31");

        assertThat(rows).extracting(ReceivableBilledCustomerDto::getCustomerRefId).doesNotHaveDuplicates();
    }
}
