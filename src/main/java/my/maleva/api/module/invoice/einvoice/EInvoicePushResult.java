package my.maleva.api.module.invoice.einvoice;

import lombok.Builder;

import java.util.List;

/**
 * Outcome of one e-invoice push (or status refresh), shaped for the legacy
 * response contract: once the invoice is committed locally, LHDN saying no is
 * not an HTTP failure of this API — it is a successful call whose body says
 * {@code IsSuccess=false} with the reasons. Only local problems (missing row,
 * wrong company) carry an HTTP error status.
 *
 * @param outcome        what happened
 * @param uuid           LHDN's document UUID, once accepted
 * @param submissionUid  LHDN's submission UUID, once accepted
 * @param longId         the long id that forms the share link, once validated
 * @param status         LHDN's document status: Submitted, Valid, Invalid, Cancelled
 * @param shareUrl       the public link for the QR, when validated
 * @param qrPngBase64    the QR as a base64 PNG, when validated
 * @param message        one line for the operator
 * @param details        the individual reasons, when there are several
 * @param errorStatus    HTTP status for a local precondition failure; null otherwise
 */
@Builder
public record EInvoicePushResult(
        Outcome outcome,
        String uuid,
        String submissionUid,
        String longId,
        String status,
        String shareUrl,
        String qrPngBase64,
        String message,
        List<String> details,
        Integer errorStatus) {

    public enum Outcome {
        /** Accepted by LHDN for validation; the UUID is saved. */
        SUBMITTED,
        /** No document sent: the invoice already has a UUID. Its status may have been re-read. */
        ALREADY_SUBMITTED,
        /** Nothing sent: the invoice failed local checks. */
        VALIDATION_FAILED,
        /** Sent, and LHDN refused it; nothing saved. */
        REJECTED,
        /** Could not reach LHDN, or LHDN answered with an error; nothing saved. */
        TRANSPORT_FAILED,
        /** Status was read back from LHDN (no submission). */
        STATUS_REFRESHED,
        /** A local precondition failed: not found, wrong company, disabled. */
        LOCAL_ERROR
    }

    public boolean success() {
        return outcome == Outcome.SUBMITTED || outcome == Outcome.ALREADY_SUBMITTED
                || outcome == Outcome.STATUS_REFRESHED;
    }

    /** True when LHDN has validated the document as Invalid. */
    public boolean lhdnInvalid() {
        return "Invalid".equalsIgnoreCase(status);
    }

    public static EInvoicePushResult localError(int httpStatus, String message) {
        return EInvoicePushResult.builder()
                .outcome(Outcome.LOCAL_ERROR).message(message).errorStatus(httpStatus).build();
    }

    public static EInvoicePushResult validationFailed(String invoiceNo, List<EInvoiceProblem> problems) {
        List<String> details = problems.stream().map(EInvoiceProblem::message).toList();
        return EInvoicePushResult.builder()
                .outcome(Outcome.VALIDATION_FAILED)
                .message("Invoice " + invoiceNo + " cannot be e-invoiced yet: " + details.size()
                        + (details.size() == 1 ? " problem" : " problems") + " to fix first")
                .details(details)
                .build();
    }

    public static EInvoicePushResult rejected(String message) {
        return EInvoicePushResult.builder().outcome(Outcome.REJECTED).message(message).build();
    }

    public static EInvoicePushResult transportFailed(String message) {
        return EInvoicePushResult.builder().outcome(Outcome.TRANSPORT_FAILED).message(message).build();
    }
}
