package my.maleva.api.module.salecreditmaster.print;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.invoice.einvoice.EInvoicePushResult;
import my.maleva.api.module.salecreditmaster.einvoice.SaleCreditEInvoiceService;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditMasterRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Before a credit note is printed, completes its LHDN record if it is only
 * half there — the port of the backfill at the top of the legacy
 * {@code Printfunction}, which both the CreditNote and the E-Invoice prints ran.
 *
 * <p>A note that was submitted (it has a document UUID) but whose long id,
 * status or validated time is still missing gets one status read from LHDN,
 * and whatever LHDN now says is saved. The print then shows the status and,
 * once the long id exists, the QR. A note never submitted, or one whose record
 * is already complete, is left alone: printing must never become a reason to
 * call LHDN.
 *
 * <p>Differences from legacy, on purpose: a failed read records nothing
 * (legacy overwrote the row with an empty long id and "now" as the validated
 * time), and a failure never stops the print.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreditNotePrintEInvoiceBackfill {

    private final SaleCreditMasterRepository creditNotes;
    private final SaleCreditEInvoiceService eInvoiceService;

    /**
     * The refresh runs here, never on the thread rendering the PDF — the same
     * fix the invoice print needed. This is a call to LHDN with a two-minute
     * timeout, and the printed note does not need it.
     */
    private final ExecutorService refreshPool = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "creditnote-backfill");
        thread.setDaemon(true);
        return thread;
    });

    /** Notes already being refreshed, so repeated prints queue one read. */
    private final Set<Integer> inFlight = ConcurrentHashMap.newKeySet();

    /**
     * Starts the status read without waiting for it.
     *
     * <p>The PDF is rendered from what is stored; when a status was genuinely
     * missing the read lands moments later and the next print carries it.
     */
    public void refreshInBackground(Integer creditNoteId, Integer companyId) {
        if (creditNoteId == null || companyId == null) {
            return;
        }
        SaleCreditMaster note = creditNotes.findById(creditNoteId).orElse(null);
        if (note == null || !Objects.equals(note.getCompanyRefId(), companyId) || !needsRefresh(note)) {
            return;
        }
        if (!inFlight.add(creditNoteId)) {
            return;
        }
        try {
            refreshPool.execute(() -> {
                try {
                    ensureStatusKnown(creditNoteId, companyId);
                } finally {
                    inFlight.remove(creditNoteId);
                }
            });
        } catch (RuntimeException ex) {
            inFlight.remove(creditNoteId);
            log.warn("Could not schedule the LHDN status backfill for credit note {}", creditNoteId, ex);
        }
    }

    @PreDestroy
    void shutdown() {
        refreshPool.shutdown();
        try {
            if (!refreshPool.awaitTermination(5, TimeUnit.SECONDS)) {
                refreshPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            refreshPool.shutdownNow();
        }
    }

    /**
     * Reads LHDN's status for the credit note when its stored record is
     * incomplete. Returns true when a read was attempted and succeeded.
     */
    public boolean ensureStatusKnown(Integer creditNoteId, Integer companyId) {
        SaleCreditMaster note = creditNotes.findById(creditNoteId).orElse(null);
        if (note == null || !Objects.equals(note.getCompanyRefId(), companyId) || !needsRefresh(note)) {
            return false;
        }
        try {
            EInvoicePushResult result = eInvoiceService.refreshStatus(creditNoteId, companyId);
            if (result.success()) {
                log.info("Credit note {}: LHDN status backfilled before print: {}",
                        note.getCNumberDisplay(), result.message());
                return true;
            }
            log.warn("Credit note {}: LHDN status could not be backfilled before print: {}",
                    note.getCNumberDisplay(), result.message());
        } catch (RuntimeException ex) {
            log.warn("Credit note {}: LHDN status backfill before print failed", note.getCNumberDisplay(), ex);
        }
        return false;
    }

    /** Legacy rule: a UUID exists but the long id, status or validated time does not. */
    static boolean needsRefresh(SaleCreditMaster note) {
        if (isBlank(note.getEInvoiceUid())) {
            return false;
        }
        return isBlank(note.getEInvoiceLongId())
                || isBlank(note.getEInvoiceStatus())
                || note.getEInvoicePushVDT() == null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
