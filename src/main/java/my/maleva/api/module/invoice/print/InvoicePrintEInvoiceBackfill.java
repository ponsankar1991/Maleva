package my.maleva.api.module.invoice.print;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.invoice.einvoice.EInvoicePushResult;
import my.maleva.api.module.invoice.einvoice.SaleInvoiceEInvoiceService;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Before an invoice is printed, completes its LHDN record if it is only
 * half there — the port of the backfill at the top of the legacy
 * {@code Printfunction}, which both the Invoice and the E-Invoice prints ran.
 *
 * <p>An invoice that was submitted (it has a document UUID) but whose long
 * id, status or validated time is still missing gets one status read from
 * LHDN, and whatever LHDN now says is saved. The print then shows the status
 * and, once the long id exists, the QR. An invoice never submitted, or one
 * whose record is already complete, is left alone: printing must never
 * become a reason to call LHDN.
 *
 * <p>Differences from legacy, on purpose: a failed read records nothing
 * (legacy overwrote the row with an empty long id and "now" as the validated
 * time), and a failure never stops the print — the paper comes out with
 * what is known and the reason goes to the log.
 */
@Component
@RequiredArgsConstructor
public class InvoicePrintEInvoiceBackfill {

    private static final Logger log = LoggerFactory.getLogger(InvoicePrintEInvoiceBackfill.class);

    private final SaleMasterRepository saleMasters;
    private final SaleInvoiceEInvoiceService eInvoiceService;

    /**
     * The refresh runs here, never on the thread rendering the PDF.
     *
     * <p>It used to run inline, and it is a call to LHDN with a 120-second
     * timeout: printing an invoice whose status was not yet stored waited for
     * a government API before the first byte of a PDF that does not need it.
     * That was the slow report. Two threads is ample — the read is small and
     * only happens for invoices whose record is still incomplete.
     */
    private final ExecutorService refreshPool = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "einvoice-backfill");
        thread.setDaemon(true);
        return thread;
    });

    /** Invoices already being refreshed, so ten prints do not queue ten LHDN reads. */
    private final Set<Integer> inFlight = ConcurrentHashMap.newKeySet();

    /**
     * Starts the status read without waiting for it, and returns at once.
     *
     * <p>This is what the print calls. The PDF is rendered from what is
     * stored, which is complete for every invoice that has been printed
     * before; when a status was genuinely missing, the read lands moments
     * later and the next print carries the status and the QR. The paper is
     * never held behind a government API.
     */
    public void refreshInBackground(Integer invoiceId, Integer companyId) {
        if (invoiceId == null || companyId == null) {
            return;
        }
        SaleMaster invoice = saleMasters.findById(invoiceId).orElse(null);
        // The common case, decided with one cheap read: nothing to do at all.
        if (invoice == null || !Objects.equals(invoice.getCompanyRefId(), companyId) || !needsRefresh(invoice)) {
            return;
        }
        if (!inFlight.add(invoiceId)) {
            return;
        }
        try {
            refreshPool.execute(() -> {
                try {
                    ensureStatusKnown(invoiceId, companyId);
                } finally {
                    inFlight.remove(invoiceId);
                }
            });
        } catch (RuntimeException ex) {
            inFlight.remove(invoiceId);
            log.warn("Could not schedule the LHDN status backfill for invoice {}", invoiceId, ex);
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
     * Reads LHDN's status for the invoice when its stored record is
     * incomplete. Returns true when a read was attempted and succeeded.
     */
    public boolean ensureStatusKnown(Integer invoiceId, Integer companyId) {
        SaleMaster invoice = saleMasters.findById(invoiceId).orElse(null);
        if (invoice == null || !Objects.equals(invoice.getCompanyRefId(), companyId)) {
            return false;
        }
        if (!needsRefresh(invoice)) {
            return false;
        }
        try {
            EInvoicePushResult result = eInvoiceService.refreshStatus(invoiceId, companyId);
            if (result.success()) {
                log.info("Invoice {}: LHDN status backfilled before print: {}", invoice.getCNumberDisplay(), result.message());
                return true;
            }
            log.warn("Invoice {}: LHDN status could not be backfilled before print: {}",
                    invoice.getCNumberDisplay(), result.message());
        } catch (RuntimeException ex) {
            log.warn("Invoice {}: LHDN status backfill before print failed", invoice.getCNumberDisplay(), ex);
        }
        return false;
    }

    /** Legacy rule: a UUID exists but the long id, status or validated time does not. */
    static boolean needsRefresh(SaleMaster invoice) {
        if (isBlank(invoice.getEInvoiceUid())) {
            return false;
        }
        return isBlank(invoice.getEInvoiceLongId())
                || isBlank(invoice.getEInvoiceStatus())
                || invoice.getEInvoicePushVDT() == null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
