package my.maleva.api.module.invoice.einvoice;

import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.MyInvoisProperties;
import my.maleva.api.integration.myinvois.MyInvoisCall;
import my.maleva.api.integration.myinvois.MyInvoisClient;
import my.maleva.api.integration.myinvois.MyInvoisDocumentCodec;
import my.maleva.api.integration.myinvois.MyInvoisErrors;
import my.maleva.api.integration.myinvois.MyInvoisGateway;
import my.maleva.api.integration.myinvois.MyInvoisQrCode;
import my.maleva.api.integration.myinvois.MyInvoisUrls;
import my.maleva.api.integration.myinvois.dto.DocumentSubmissionResponse;
import my.maleva.api.integration.myinvois.dto.SubmissionStatusResponse;
import my.maleva.api.integration.myinvois.ubl.UblDocument;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pushes a sale invoice to LHDN MyInvois — the Java port of the legacy
 * {@code SaleInvoiceServices.EInvoiceConvert}.
 *
 * <p>The steps, in order, and why the order matters:
 * <ol>
 *   <li><b>Load</b> the invoice and everything joined to it into a snapshot.</li>
 *   <li><b>Already submitted?</b> Then do not build or send anything: re-read
 *       the LHDN status if it is not yet final (as the legacy button did on a
 *       second click), record it, and answer with the share link and QR. An
 *       Invalid document may be resubmitted when configuration allows.</li>
 *   <li><b>Validate</b> — completeness and money — and stop with every problem
 *       listed. Nothing has left the building yet.</li>
 *   <li><b>Build</b> the UBL document, <b>encode</b> it (one byte array → hash
 *       and Base64), <b>submit</b> it.</li>
 *   <li>On acceptance, <b>save the UUID immediately</b> in its own transaction.
 *       Legacy saved it only after the follow-up status call; an error there
 *       lost the UUID and the next click created a duplicate government
 *       document for the same invoice.</li>
 *   <li><b>Read the status once</b>. LHDN validates asynchronously, so this
 *       usually says "Submitted"; whatever LHDN reports — long id, validated
 *       time, status — is saved as reported, never a placeholder "now".</li>
 * </ol>
 *
 * <p>Deliberately preserved from legacy: the issue date/time is the moment of
 * submission in UTC, not the sale date — LHDN rejects documents issued outside
 * its submission window, and this is what lets a back-dated invoice be sent.
 */
@Slf4j
@Service
public class SaleInvoiceEInvoiceService {

    /** All local timestamps in this database are Malaysian wall-clock time. */
    static final ZoneId MALAYSIA = ZoneId.of("Asia/Kuala_Lumpur");

    static final String STATUS_SUBMITTED = "Submitted";
    static final String STATUS_VALID = "Valid";
    static final String STATUS_INVALID = "Invalid";
    static final String STATUS_CANCELLED = "Cancelled";

    private final MyInvoisProperties properties;
    private final EInvoiceSnapshotLoader loader;
    private final EInvoiceValidator validator;
    private final EInvoiceDocumentBuilder builder;
    private final MyInvoisDocumentCodec codec;
    private final MyInvoisGateway gateway;
    private final MyInvoisUrls urls;
    private final MyInvoisQrCode qrCode;
    private final SaleMasterRepository saleMasters;
    private final Clock clock;

    /** Invoices currently being submitted, so a double-click cannot submit twice. */
    private final Set<Integer> inFlight = ConcurrentHashMap.newKeySet();

    @Autowired
    public SaleInvoiceEInvoiceService(MyInvoisProperties properties,
                                      EInvoiceSnapshotLoader loader,
                                      EInvoiceValidator validator,
                                      EInvoiceDocumentBuilder builder,
                                      MyInvoisDocumentCodec codec,
                                      MyInvoisGateway gateway,
                                      MyInvoisUrls urls,
                                      MyInvoisQrCode qrCode,
                                      SaleMasterRepository saleMasters) {
        this(properties, loader, validator, builder, codec, gateway, urls, qrCode, saleMasters,
                Clock.system(MALAYSIA));
    }

    /** Visible for tests: a fixed clock makes the issue time and the stored push time deterministic. */
    SaleInvoiceEInvoiceService(MyInvoisProperties properties,
                               EInvoiceSnapshotLoader loader,
                               EInvoiceValidator validator,
                               EInvoiceDocumentBuilder builder,
                               MyInvoisDocumentCodec codec,
                               MyInvoisGateway gateway,
                               MyInvoisUrls urls,
                               MyInvoisQrCode qrCode,
                               SaleMasterRepository saleMasters,
                               Clock clock) {
        this.properties = properties;
        this.loader = loader;
        this.validator = validator;
        this.builder = builder;
        this.codec = codec;
        this.gateway = gateway;
        this.urls = urls;
        this.qrCode = qrCode;
        this.saleMasters = saleMasters;
        this.clock = clock;
    }

    // ─────────────────────────────────────────────────────────────── push ──

    public EInvoicePushResult push(Integer invoiceId, Integer companyId) {
        EInvoicePushResult notReady = precondition();
        if (notReady != null) {
            return notReady;
        }
        if (!inFlight.add(invoiceId)) {
            return EInvoicePushResult.localError(409,
                    "Invoice " + invoiceId + " is already being submitted; wait for that to finish");
        }
        try {
            return doPush(invoiceId, companyId);
        } finally {
            inFlight.remove(invoiceId);
        }
    }

    private EInvoicePushResult doPush(Integer invoiceId, Integer companyId) {
        Optional<EInvoiceSnapshot> loaded = loader.load(invoiceId, companyId);
        if (loaded.isEmpty()) {
            return EInvoicePushResult.localError(404,
                    "Invoice " + invoiceId + " was not found for company " + companyId);
        }
        EInvoiceSnapshot snapshot = loaded.get();
        EInvoiceSnapshot.Header header = snapshot.header();

        if (header.alreadySubmitted() && !mayResubmit(header)) {
            return alreadySubmitted(header, invoiceId, companyId);
        }

        List<EInvoiceProblem> problems = validator.validate(snapshot);
        if (!problems.isEmpty()) {
            log.info("Invoice {} refused by e-invoice validation: {}", header.invoiceNo(),
                    problems.stream().map(EInvoiceProblem::code).toList());
            return EInvoicePushResult.validationFailed(header.invoiceNo(), problems);
        }

        Instant issuedAt = clock.instant();
        UblDocument document = builder.build(snapshot, issuedAt);
        MyInvoisDocumentCodec.EncodedDocument encoded = codec.encode(document, header.invoiceNo());

        MyInvoisCall<DocumentSubmissionResponse> call = gateway.submit(encoded.toSubmissionRequest(), companyId);
        if (!call.success()) {
            if (call.result().success()) {
                // LHDN answered 2xx but the body could not be read: the document
                // may well be accepted. A blind retry would submit it twice.
                log.error("Invoice {}: LHDN answered HTTP {} but the reply could not be read: {}",
                        header.invoiceNo(), call.result().status(), call.result().body());
                return EInvoicePushResult.transportFailed("LHDN accepted the request for invoice " + header.invoiceNo()
                        + " but its reply could not be read; check the LHDN portal for this invoice before pushing again");
            }
            log.warn("Invoice {} submission failed: {}", header.invoiceNo(), call.message());
            return EInvoicePushResult.transportFailed(call.message());
        }

        DocumentSubmissionResponse response = call.data();
        if (response.getRejectedDocuments() != null && !response.getRejectedDocuments().isEmpty()) {
            String reason = MyInvoisErrors.describeRejection(response.getRejectedDocuments().get(0).getError());
            log.warn("Invoice {} rejected by LHDN:\n{}\nDocument was: {}", header.invoiceNo(), reason, encoded.json());
            return EInvoicePushResult.rejected(reason);
        }
        if (response.getAcceptedDocuments() == null || response.getAcceptedDocuments().isEmpty()
                || isBlank(response.getSubmissionUid())) {
            log.error("Invoice {}: LHDN answered 2xx but named no accepted document: {}",
                    header.invoiceNo(), call.result().body());
            return EInvoicePushResult.transportFailed(
                    "LHDN accepted the request but did not return a document UUID; check the LHDN portal before retrying");
        }

        String uuid = response.getAcceptedDocuments().get(0).getUuid();
        String submissionUid = response.getSubmissionUid();
        // Logged BEFORE the save: if the save fails this line is the only record.
        log.info("Invoice {} accepted by LHDN: uuid={} submission={}", header.invoiceNo(), uuid, submissionUid);

        LocalDateTime pushedAt = LocalDateTime.ofInstant(issuedAt, MALAYSIA);
        try {
            saleMasters.claimEInvoiceSubmission(invoiceId, companyId, uuid, submissionUid, STATUS_SUBMITTED, pushedAt);
        } catch (DataAccessException dbDown) {
            // The government has the document; our row does not know it. The
            // one thing that must not happen now is a second submission.
            log.error("Invoice {} WAS accepted by LHDN (uuid={}, submission={}) but the UUID could not be saved",
                    header.invoiceNo(), uuid, submissionUid, dbDown);
            return EInvoicePushResult.builder()
                    .outcome(EInvoicePushResult.Outcome.SUBMITTED)
                    .uuid(uuid)
                    .submissionUid(submissionUid)
                    .status(STATUS_SUBMITTED)
                    .message("Invoice " + header.invoiceNo() + " WAS accepted by LHDN (UUID " + uuid
                            + ") but could not be recorded locally — do NOT push it again; "
                            + "record the UUID on the invoice and contact support")
                    .build();
        }

        ValidationOutcome outcome = readAndRecordStatus(invoiceId, companyId, submissionUid, header.invoiceNo());

        return EInvoicePushResult.builder()
                .outcome(EInvoicePushResult.Outcome.SUBMITTED)
                .uuid(uuid)
                .submissionUid(submissionUid)
                .longId(outcome.longId())
                .status(outcome.status())
                .shareUrl(shareUrl(uuid, outcome.longId()))
                .qrPngBase64(qrFor(uuid, outcome.longId()))
                .message("Invoice " + header.invoiceNo() + " submitted to LHDN"
                        + (isBlank(outcome.status()) ? "" : " (" + outcome.status() + ")"))
                .build();
    }

    // ───────────────────────────────────────────────────────────── status ──

    /**
     * Re-reads the LHDN status of an already-submitted invoice and records
     * whatever is now known. This is the port of the long-id backfill that
     * legacy ran from the print path; here it is its own call so a print can
     * never re-poll LHDN as a side effect.
     */
    public EInvoicePushResult refreshStatus(Integer invoiceId, Integer companyId) {
        EInvoicePushResult notReady = precondition();
        if (notReady != null) {
            return notReady;
        }
        Optional<EInvoiceSnapshot> loaded = loader.load(invoiceId, companyId);
        if (loaded.isEmpty()) {
            return EInvoicePushResult.localError(404,
                    "Invoice " + invoiceId + " was not found for company " + companyId);
        }
        EInvoiceSnapshot.Header header = loaded.get().header();
        if (!header.alreadySubmitted()) {
            return EInvoicePushResult.localError(409,
                    "Invoice " + header.invoiceNo() + " has not been submitted to LHDN");
        }
        if (isBlank(header.eInvoiceSubmissionUid())) {
            return EInvoicePushResult.localError(409,
                    "Invoice " + header.invoiceNo() + " has a document UUID but no submission id; its status cannot be read");
        }

        ValidationOutcome outcome;
        if (isFinal(header.eInvoiceStatus(), header.eInvoiceLongId())) {
            // Nothing more will change at LHDN; answer from what is stored.
            outcome = new ValidationOutcome(header.eInvoiceStatus(),
                    isBlank(header.eInvoiceLongId()) ? null : header.eInvoiceLongId(), null);
        } else {
            outcome = readAndRecordStatus(invoiceId, companyId, header.eInvoiceSubmissionUid(), header.invoiceNo());
            if (outcome.failure() != null) {
                return EInvoicePushResult.transportFailed(outcome.failure());
            }
        }

        return EInvoicePushResult.builder()
                .outcome(EInvoicePushResult.Outcome.STATUS_REFRESHED)
                .uuid(header.eInvoiceUid())
                .submissionUid(header.eInvoiceSubmissionUid())
                .longId(outcome.longId())
                .status(outcome.status())
                .shareUrl(shareUrl(header.eInvoiceUid(), outcome.longId()))
                .qrPngBase64(qrFor(header.eInvoiceUid(), outcome.longId()))
                .message("Invoice " + header.invoiceNo() + " is " + (isBlank(outcome.status()) ? "awaiting validation" : outcome.status()))
                .build();
    }

    /**
     * One GET of the submission. Records exactly what LHDN said: a status
     * alone while validation is pending; the long id and/or validated time
     * once either exists. On failure records nothing and says why.
     */
    private ValidationOutcome readAndRecordStatus(Integer invoiceId, Integer companyId,
                                                  String submissionUid, String invoiceNo) {
        MyInvoisCall<SubmissionStatusResponse> call = gateway.submissionStatus(submissionUid, companyId);
        if (!call.success()) {
            log.warn("Invoice {}: status read failed: {}", invoiceNo, call.message());
            return ValidationOutcome.failed(call.message());
        }
        List<SubmissionStatusResponse.DocumentSummary> summaries = call.data().getDocumentSummary();
        if (summaries == null || summaries.isEmpty()) {
            // LHDN has not finished validating. Its submission-level wording
            // ("in progress") is not a document status; the document is still
            // Submitted, and the column keeps the document vocabulary
            // (Submitted / Valid / Invalid / Cancelled) so the final-state
            // check and the screen have one set of values to read.
            saleMasters.recordEInvoiceStatus(invoiceId, companyId, STATUS_SUBMITTED);
            return new ValidationOutcome(STATUS_SUBMITTED, null, null);
        }

        SubmissionStatusResponse.DocumentSummary summary = summaries.get(0);
        String status = normaliseStatus(summary.getStatus());
        String longId = isBlank(summary.getLongId()) ? null : summary.getLongId().trim();
        LocalDateTime validatedAt = parseLhdnInstant(summary.getDateTimeValidated());
        if (validatedAt == null && !isBlank(summary.getDateTimeValidated())) {
            log.warn("Invoice {}: LHDN validated time '{}' could not be parsed; stored without it",
                    invoiceNo, summary.getDateTimeValidated());
        }

        if (longId != null || validatedAt != null) {
            saleMasters.recordEInvoiceValidation(invoiceId, companyId, longId == null ? "" : longId,
                    isBlank(status) ? STATUS_SUBMITTED : status, validatedAt);
        } else if (!isBlank(status)) {
            saleMasters.recordEInvoiceStatus(invoiceId, companyId, status);
        }
        if (STATUS_INVALID.equalsIgnoreCase(status)) {
            log.warn("Invoice {} was validated as INVALID by LHDN: {}", invoiceNo, summary.getDocumentStatusReason());
        }
        return new ValidationOutcome(status, longId, validatedAt);
    }

    // ────────────────────────────────────────────────────────────── helpers ──

    /** Local reasons nothing can be done at all: switched off, or misconfigured. */
    private EInvoicePushResult precondition() {
        if (!properties.isEnabled()) {
            return EInvoicePushResult.localError(409, MyInvoisClient.DISABLED_MESSAGE);
        }
        try {
            urls.environment();
            properties.supplier();
        } catch (IllegalArgumentException | IllegalStateException misconfigured) {
            log.error("MyInvois configuration error: {}", misconfigured.getMessage());
            return EInvoicePushResult.localError(409, "MyInvois is not configured correctly on this server: "
                    + misconfigured.getMessage());
        }
        return null;
    }

    /**
     * The answer for an invoice that already has a UUID. If LHDN's word on it
     * is not final, the status is read now, so a second click fetches the QR
     * exactly as it did in legacy. A failed read is said so, but the invoice
     * is still reported as submitted — because it is.
     */
    private EInvoicePushResult alreadySubmitted(EInvoiceSnapshot.Header header, Integer invoiceId, Integer companyId) {
        String status = header.eInvoiceStatus();
        String longId = isBlank(header.eInvoiceLongId()) ? null : header.eInvoiceLongId();
        String readFailure = null;

        if (!isFinal(status, longId) && !isBlank(header.eInvoiceSubmissionUid())) {
            ValidationOutcome refreshed = readAndRecordStatus(invoiceId, companyId,
                    header.eInvoiceSubmissionUid(), header.invoiceNo());
            if (refreshed.failure() == null) {
                status = refreshed.status();
                longId = refreshed.longId();
            } else {
                readFailure = refreshed.failure();
            }
        }

        String message = "Invoice " + header.invoiceNo() + " was already submitted to LHDN"
                + (isBlank(status) ? " and is awaiting validation" : " (" + status + ")")
                + (readFailure == null ? "" : "; its current status could not be read: " + readFailure);
        return EInvoicePushResult.builder()
                .outcome(EInvoicePushResult.Outcome.ALREADY_SUBMITTED)
                .uuid(header.eInvoiceUid())
                .submissionUid(header.eInvoiceSubmissionUid())
                .longId(longId)
                .status(status)
                .shareUrl(shareUrl(header.eInvoiceUid(), longId))
                .qrPngBase64(qrFor(header.eInvoiceUid(), longId))
                .message(message)
                .build();
    }

    private boolean mayResubmit(EInvoiceSnapshot.Header header) {
        return properties.isAllowResubmitInvalid() && STATUS_INVALID.equalsIgnoreCase(header.eInvoiceStatus());
    }

    /**
     * Whether LHDN's word on the document is final. Invalid and Cancelled are
     * final on their own; Valid is final only once its long id is stored,
     * because the long id is what the printed QR needs — a Valid row without
     * one is re-read on the next click.
     */
    static boolean isFinal(String status, String longId) {
        if (STATUS_INVALID.equalsIgnoreCase(status) || STATUS_CANCELLED.equalsIgnoreCase(status)) {
            return true;
        }
        return STATUS_VALID.equalsIgnoreCase(status) && !isBlank(longId);
    }

    /** LHDN's casing varies between endpoints ("Valid" vs "valid"); store one form. */
    static String normaliseStatus(String status) {
        if (isBlank(status)) {
            return null;
        }
        String s = status.trim();
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    /**
     * LHDN's ISO-8601 instant as Malaysian wall-clock time, or null. Accepts
     * an offset ({@code ...+08:00}), a Z, or — as LHDN has been seen to send —
     * no zone at all, which is taken as UTC.
     */
    static LocalDateTime parseLhdnInstant(String value) {
        if (isBlank(value)) {
            return null;
        }
        String v = value.trim();
        try {
            return OffsetDateTime.parse(v).atZoneSameInstant(MALAYSIA).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return Instant.parse(v).atZone(MALAYSIA).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // fall through
        }
        try {
            return LocalDateTime.parse(v).atOffset(ZoneOffset.UTC).atZoneSameInstant(MALAYSIA).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String shareUrl(String uuid, String longId) {
        return isBlank(uuid) || isBlank(longId) ? null : urls.documentShareLink(uuid, longId);
    }

    private String qrFor(String uuid, String longId) {
        String url = shareUrl(uuid, longId);
        if (url == null) {
            return null;
        }
        try {
            return Base64.getEncoder().encodeToString(qrCode.png(url));
        } catch (Exception ex) {
            // The document is valid with or without a picture of its link.
            log.warn("QR code for {} could not be rendered: {}", url, ex.getMessage());
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /** What one status read established. */
    record ValidationOutcome(String status, String longId, LocalDateTime validatedAt, String failure) {

        ValidationOutcome(String status, String longId, LocalDateTime validatedAt) {
            this(status, longId, validatedAt, null);
        }

        static ValidationOutcome failed(String failure) {
            return new ValidationOutcome(null, null, null, failure);
        }
    }
}
