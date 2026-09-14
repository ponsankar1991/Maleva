package my.maleva.api.module.invoice.service;

import my.maleva.api.common.exception.InvalidRequestException;
import my.maleva.api.module.invoice.dto.SaleInvoiceDetailRequestDTO;
import my.maleva.api.module.invoice.dto.SaleInvoiceRequestDTO;
import my.maleva.api.module.invoice.service.InvoiceJobBillingGuard.Link;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * One job, one invoice: the rule that stops a second Save, a second tab or a
 * reload from billing a job that already has an invoice.
 */
class InvoiceJobBillingGuardTest {

    private static final int COMPANY = 6;
    private static final int JOB = 20995;

    private final NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
    private final InvoiceJobBillingGuard guard = new InvoiceJobBillingGuard(jdbc);

    private static SaleInvoiceRequestDTO invoice(int id) {
        return SaleInvoiceRequestDTO.builder().id(id).companyRefId(COMPANY).build();
    }

    private static SaleInvoiceDetailRequestDTO lineFor(Integer job) {
        return SaleInvoiceDetailRequestDTO.builder().itemMasterRefId(54).saleOrderMasterRefId(job).build();
    }

    private void existingLinks(Link... links) {
        when(jdbc.query(contains("SaleMasterReference"), any(SqlParameterSource.class),
                ArgumentMatchers.<RowMapper<Link>>any()))
                .thenReturn(Arrays.asList(links));
    }

    @Test
    void collectsJobsFromHeaderReferencesAndLinesSortedAndWithoutBlanks() {
        SaleInvoiceRequestDTO request = invoice(0);
        request.setSaleOrderMasterNo(300);
        request.setSaleOrderRefIds(Arrays.asList(200, null, 0, 300));

        assertThat(InvoiceJobBillingGuard.jobsBilledBy(request,
                Arrays.asList(lineFor(100), lineFor(null), lineFor(0), lineFor(200))))
                .containsExactly(100, 200, 300);
    }

    @Test
    void anInvoiceWithNoJobsTouchesNothing() {
        guard.lockAndRequireUnbilled(invoice(0), List.of(lineFor(null)), COMPANY);
        verifyNoInteractions(jdbc);
    }

    @Test
    void aSecondInvoiceForABilledJobIsRefusedAndNamesBothNumbers() {
        existingLinks(new Link(JOB, "TR2600123", 44358, "INV000044358"));

        assertThatThrownBy(() -> guard.lockAndRequireUnbilled(invoice(0), List.of(lineFor(JOB)), COMPANY))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("job TR2600123 is already on invoice INV000044358");
    }

    @Test
    void theJobRowsAreLockedBeforeTheLinksAreRead() {
        existingLinks();

        guard.lockAndRequireUnbilled(invoice(0), List.of(lineFor(JOB)), COMPANY);

        InOrder order = inOrder(jdbc);
        order.verify(jdbc).queryForList(contains("UPDLOCK, HOLDLOCK"), any(SqlParameterSource.class), eq(Integer.class));
        order.verify(jdbc).query(contains("SaleMasterReference"), any(SqlParameterSource.class),
                ArgumentMatchers.<RowMapper<Link>>any());
    }

    @Test
    void anUnbilledJobSaves() {
        existingLinks();
        assertThatCode(() -> guard.lockAndRequireUnbilled(invoice(0), List.of(lineFor(JOB)), COMPANY))
                .doesNotThrowAnyException();
    }

    @Test
    void editingAnInvoiceIsNotBlockedByItsOwnJobs() {
        existingLinks(new Link(JOB, "TR2600123", 44358, "INV000044358"));
        assertThatCode(() -> guard.lockAndRequireUnbilled(invoice(44358), List.of(lineFor(JOB)), COMPANY))
                .doesNotThrowAnyException();
    }

    @Test
    void anEditKeepsAJobItAlreadyHadEvenWhenAnOlderDuplicateClaimsIt() {
        List<Link> links = List.of(
                new Link(JOB, "TR2600123", 44300, "INV000044300"),
                new Link(JOB, "TR2600123", 44358, "INV000044358"));
        assertThat(InvoiceJobBillingGuard.conflicts(links, 44358)).isEmpty();
    }

    @Test
    void anEditCannotAddAJobThatAnotherInvoiceHas() {
        List<Link> links = List.of(
                new Link(JOB, "TR2600123", 44358, "INV000044358"),
                new Link(21000, "TR2600200", 44400, "INV000044400"));
        assertThat(InvoiceJobBillingGuard.conflicts(links, 44358))
                .extracting(Link::jobNo).containsExactly("TR2600200");
    }
}
