package my.maleva.api.module.invoice.print;

import lombok.RequiredArgsConstructor;
import my.maleva.api.module.invoice.einvoice.EInvoicePushResult;
import my.maleva.api.module.invoice.einvoice.SaleInvoiceEInvoiceService;
import my.maleva.api.module.invoice.entity.SaleMaster;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
