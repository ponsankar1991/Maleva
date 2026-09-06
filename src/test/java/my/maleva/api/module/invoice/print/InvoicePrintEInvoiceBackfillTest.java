package my.maleva.api.module.invoice.print;

import my.maleva.api.module.invoice.einvoice.EInvoicePushResult;
import my.maleva.api.module.invoice.einvoice.SaleInvoiceEInvoiceService;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The legacy print re-read LHDN only for a submitted invoice with a gap in
 * its record. Pins that rule and that a failed read never blocks the print.
 */
class InvoicePrintEInvoiceBackfillTest {

    private final SaleMasterRepository saleMasters = mock(SaleMasterRepository.class);
    private final SaleInvoiceEInvoiceService eInvoiceService = mock(SaleInvoiceEInvoiceService.class);
    private final InvoicePrintEInvoiceBackfill backfill = new InvoicePrintEInvoiceBackfill(saleMasters, eInvoiceService);

    private SaleMaster invoice(String uid, String longId, String status, LocalDateTime validated) {
        SaleMaster sm = new SaleMaster();
        sm.setId(15897);
        sm.setCompanyRefId(6);
        sm.setCNumberDisplay("INV000044006");
        sm.setEInvoiceUid(uid);
        sm.setEInvoiceLongId(longId);
        sm.setEInvoiceStatus(status);
        sm.setEInvoicePushVDT(validated);
        when(saleMasters.findById(15897)).thenReturn(Optional.of(sm));
        return sm;
    }

    @Test
    void neverSubmittedIsLeftAlone() {
        invoice("", "", "", null);
        assertThat(backfill.ensureStatusKnown(15897, 6)).isFalse();
        verify(eInvoiceService, never()).refreshStatus(anyInt(), anyInt());
    }

    @Test
    void completeRecordIsLeftAlone() {
        invoice("8TCBM57R389YDSAVW6YEH01M10", "YQH73576FY9VR57B", "Valid", LocalDateTime.of(2026, 8, 27, 10, 40));
        assertThat(backfill.ensureStatusKnown(15897, 6)).isFalse();
        verify(eInvoiceService, never()).refreshStatus(anyInt(), anyInt());
    }

    @Test
    void submittedWithAGapIsReadAndRecorded() {
        invoice("8TCBM57R389YDSAVW6YEH01M10", "", "", LocalDateTime.of(2026, 8, 27, 10, 40));
        when(eInvoiceService.refreshStatus(15897, 6)).thenReturn(EInvoicePushResult.builder()
                .outcome(EInvoicePushResult.Outcome.STATUS_REFRESHED).status("Valid").message("Invoice INV000044006 is Valid").build());

        assertThat(backfill.ensureStatusKnown(15897, 6)).isTrue();
        verify(eInvoiceService).refreshStatus(15897, 6);
    }

    @Test
    void aFailedReadDoesNotStopThePrint() {
        invoice("8TCBM57R389YDSAVW6YEH01M10", "", "Valid", null);
        when(eInvoiceService.refreshStatus(15897, 6))
                .thenReturn(EInvoicePushResult.transportFailed("LHDN did not answer"));
        assertThat(backfill.ensureStatusKnown(15897, 6)).isFalse();

        when(eInvoiceService.refreshStatus(15897, 6)).thenThrow(new IllegalStateException("boom"));
        assertThat(backfill.ensureStatusKnown(15897, 6)).isFalse();
    }

    @Test
    void anotherCompanysInvoiceIsIgnored() {
        invoice("8TCBM57R389YDSAVW6YEH01M10", "", "", null);
        assertThat(backfill.ensureStatusKnown(15897, 1)).isFalse();
        verify(eInvoiceService, never()).refreshStatus(anyInt(), anyInt());
    }
}
