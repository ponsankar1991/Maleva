package my.maleva.api.module.salecreditmaster.print;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.module.invoice.einvoice.EInvoicePushResult;
import my.maleva.api.module.salecreditmaster.einvoice.SaleCreditEInvoiceService;
import my.maleva.api.module.salecreditmaster.entity.SaleCreditMaster;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditMasterRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;

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
