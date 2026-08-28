package my.maleva.api.module.transactionreport.service.impl;

import my.maleva.api.module.transactionreport.dto.PaymentDoneRequestDto;
import my.maleva.api.module.transactionreport.dto.PaymentDoneRowDto;
import my.maleva.api.module.transactionreport.dto.PaymentDoneViewDto;
import my.maleva.api.module.transactionreport.repository.TransactionReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * What the Payment Completed service does to a request before the SQL sees it.
 *
 * The query itself is exercised against the database, not here; these pin the
 * decisions that would otherwise be invisible — how the day is bracketed, what
 * an empty category list means, and that the total is the repository's SUM and
 * never a re-add of the rows.
 */
@ExtendWith(MockitoExtension.class)
class TransactionReportServiceImplTest {

    @Mock
    private TransactionReportRepository repository;

    @InjectMocks
    private TransactionReportServiceImpl service;

    private static PaymentDoneRowDto row(int id, String amount) {
        return PaymentDoneRowDto.builder()
                .id(id)
                .cNumberDisplay("PV0000" + id)
                .amount(new BigDecimal(amount))
                .detailedId(0)
                .build();
    }

    private void stub(List<PaymentDoneRowDto> rows, String total) {
        when(repository.findCompletedPayments(anyInt(), anyString(), anyString(), any(), any(), any()))
                .thenReturn(rows);
        when(repository.sumCompletedPayments(anyInt(), anyString(), anyString(), any(), any(), any()))
                .thenReturn(new BigDecimal(total));
    }

    @Test
    void bracketsBothBoundsToWholeDays() {
        stub(List.of(), "0");

        service.getCompletedPayments(PaymentDoneRequestDto.builder()
                .comid(6)
                .fromDate("2026-08-01")
                .toDate("2026-08-28")
                .build());

        ArgumentCaptor<String> from = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> to = ArgumentCaptor.forClass(String.class);
        verify(repository).findCompletedPayments(eq(6), from.capture(), to.capture(), any(), any(), any());

        // A payment stamped late on the To date still counts — legacy bracketed
        // the range this way and the migrated dashboard copy does too.
        assertEquals("2026-08-01 00:00:00", from.getValue());
        assertEquals("2026-08-28 23:59:59", to.getValue());
    }

    @Test
    void acceptsTheLegacyDayFirstDateFormat() {
        stub(List.of(), "0");

        service.getCompletedPayments(PaymentDoneRequestDto.builder()
                .comid(6)
                .fromDate("01/08/2026")
                .toDate("28/08/2026")
                .build());

        ArgumentCaptor<String> from = ArgumentCaptor.forClass(String.class);
        verify(repository).findCompletedPayments(eq(6), from.capture(), anyString(), any(), any(), any());

        // dd/MM/yyyy, the order the legacy date pickers posted — not MM/dd.
        assertEquals("2026-08-01 00:00:00", from.getValue());
    }

    @Test
    void fallsBackToTheCurrentMonthRatherThanFailingOnAnUnparseableDate() {
        stub(List.of(), "0");

        service.getCompletedPayments(PaymentDoneRequestDto.builder()
                .comid(6)
                .fromDate("not a date")
                .toDate(null)
                .build());

        ArgumentCaptor<String> from = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> to = ArgumentCaptor.forClass(String.class);
        verify(repository).findCompletedPayments(eq(6), from.capture(), to.capture(), any(), any(), any());

        assertEquals(LocalDate.now().withDayOfMonth(1) + " 00:00:00", from.getValue());
        assertEquals(LocalDate.now() + " 23:59:59", to.getValue());
    }

    @Test
    void treatsBlanksAndDuplicateCategoriesAsNoise() {
        stub(List.of(), "0");

        service.getCompletedPayments(PaymentDoneRequestDto.builder()
                .comid(6)
                .fromDate("2026-08-01")
                .toDate("2026-08-28")
                .descriptions(Arrays.asList("FUEL", "  ", " FUEL ", null, "TOLL"))
                .build());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> categories = ArgumentCaptor.forClass(List.class);
        verify(repository).findCompletedPayments(anyInt(), anyString(), anyString(), any(), any(), categories.capture());

        // A list of blanks would otherwise become `Description IN ('')`, which
        // matches nothing and reads on screen as an empty month.
        assertEquals(List.of("FUEL", "TOLL"), categories.getValue());
    }

    @Test
    void anEmptyCategoryListMeansEveryCategory() {
        stub(List.of(), "0");

        service.getCompletedPayments(PaymentDoneRequestDto.builder()
                .comid(6)
                .fromDate("2026-08-01")
                .toDate("2026-08-28")
                .descriptions(Collections.emptyList())
                .build());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> categories = ArgumentCaptor.forClass(List.class);
        verify(repository).findCompletedPayments(anyInt(), anyString(), anyString(), any(), any(), categories.capture());

        assertTrue(categories.getValue().isEmpty());
    }

    @Test
    void reportsTheRepositorySumRatherThanReAddingTheRows() {
        // The rows add up to 15,500.75; the repository says 15,500.99. The
        // repository wins — it summed the exact numeric columns in SQL, and
        // re-adding rows in floating point is what drifted the payments total.
        stub(List.of(row(1, "12000.00"), row(2, "3500.75")), "15500.99");

        PaymentDoneViewDto view = service.getCompletedPayments(PaymentDoneRequestDto.builder()
                .comid(6)
                .fromDate("2026-08-01")
                .toDate("2026-08-28")
                .build());

        assertEquals(0, new BigDecimal("15500.99").compareTo(view.getTotalAmount()));
        assertEquals(2, view.getCount());
        assertEquals(2, view.getRows().size());
    }
}
