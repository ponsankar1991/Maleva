package my.maleva.api.module.salecreditmaster.einvoice;

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
import my.maleva.api.module.invoice.einvoice.EInvoiceDocumentBuilder;
import my.maleva.api.module.invoice.einvoice.EInvoiceDocumentKind;
import my.maleva.api.module.invoice.einvoice.EInvoiceProblem;
import my.maleva.api.module.invoice.einvoice.EInvoicePushResult;
import my.maleva.api.module.invoice.einvoice.EInvoiceSnapshot;
import my.maleva.api.module.invoice.einvoice.EInvoiceStatus;
import my.maleva.api.module.invoice.einvoice.EInvoiceValidator;
import my.maleva.api.module.salecreditmaster.repository.SaleCreditMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Submits a credit note to LHDN MyInvois — the Java port of the legacy
 * {@code SaleCreditServices.EInvoiceCreditConvert}.
 *
 * <p>Structurally a credit note is an invoice with document type 02 and a
 * billing reference back to the invoice it corrects, so it is validated and
 * built by the same {@link EInvoiceValidator} and {@link EInvoiceDocumentBuilder}
 * the sale invoice uses; only the loader and this orchestration are its own.
 *
 * <p>Differences from legacy, each of which was a defect there:
 * <ul>
 *   <li>The UUID is saved the moment LHDN accepts the document, in its own
 *       transaction. Legacy saved it only after the follow-up status call, so
 *       any error in between lost it and the next click submitted a second
 *       government document for the same credit note.</li>
 *   <li>The billing reference sends the corrected invoice's UUID only when
 *       there is one. Legacy always emitted the element, with an empty value
 *       for an invoice that had never been e-invoiced.</li>
 *   <li>A validated time is stored only when LHDN reports one. Legacy
 *       defaulted it to {@code DateTime.Now} whenever the status read failed,
 *       so notes carried a validation timestamp for a validation that had not
 *       happened.</li>
 *   <li>Two clicks cannot submit twice: a submission in flight is refused.</li>
 * </ul>
 *
 * <p>Kept from legacy on purpose: the issue date and time are the moment of
 * submission in UTC, not the credit note's date. LHDN rejects documents issued
 * outside its submission window, and this is what lets a back-dated note go.
 */
@Slf4j
@Service
public class SaleCreditEInvoiceService {

    private final MyInvoisProperties properties;
    private final SaleCreditEInvoiceSnapshotLoader loader;
    private final EInvoiceValidator validator;
    private final EInvoiceDocumentBuilder builder;
    private final MyInvoisDocumentCodec codec;
    private final MyInvoisGateway gateway;
    private final MyInvoisUrls urls;
    private final MyInvoisQrCode qrCode;
    private final SaleCreditMasterRepository creditNotes;
    private final Clock clock;

    /** Credit notes currently being submitted, so a double-click cannot submit twice. */
    private final Set<Integer> inFlight = ConcurrentHashMap.newKeySet();

    @Autowired
    public SaleCreditEInvoiceService(MyInvoisProperties properties,
                                     SaleCreditEInvoiceSnapshotLoader loader,
                                     EInvoiceValidator validator,
                                     EInvoiceDocumentBuilder builder,
                                     MyInvoisDocumentCodec codec,
                                     MyInvoisGateway gateway,
                                     MyInvoisUrls urls,
                                     MyInvoisQrCode qrCode,
                                     SaleCreditMasterRepository creditNotes) {
        this(properties, loader, validator, builder, codec, gateway, urls, qrCode, creditNotes,
                Clock.system(EInvoiceStatus.MALAYSIA));
    }

    /** Visible for tests: a fixed clock makes the issue time deterministic. */
    SaleCreditEInvoiceService(MyInvoisProperties properties,
                              SaleCreditEInvoiceSnapshotLoader loader,
                              EInvoiceValidator validator,
                              EInvoiceDocumentBuilder builder,
                              MyInvoisDocumentCodec codec,
                              MyInvoisGateway gateway,
                              MyInvoisUrls urls,
                              MyInvoisQrCode qrCode,
                              SaleCreditMasterRepository creditNotes,
                              Clock clock) {
        this.properties = properties;
        this.loader = loader;
        this.validator = validator;
        this.builder = builder;
        this.codec = codec;
        this.gateway = gateway;
        this.urls = urls;
        this.qrCode = qrCode;
        this.creditNotes = creditNotes;
        this.clock = clock;
    }

    // ─────────────────────────────────────────────────────────────── push ──

    public EInvoicePushResult push(Integer creditNoteId, Integer companyId) {
        EInvoicePushResult notReady = precondition();
        if (notReady != null) {
            return notReady;
        }
        if (!inFlight.add(creditNoteId)) {
            return EInvoicePushResult.localError(409,
                    "Credit note " + creditNoteId + " is already being submitted; wait for that to finish");
        }
        try {
            return doPush(creditNoteId, companyId);
        } finally {
            inFlight.remove(creditNoteId);
        }
    }

    private EInvoicePushResult doPush(Integer creditNoteId, Integer companyId) {
        Optional<SaleCreditEInvoiceSnapshotLoader.Loaded> found = loader.load(creditNoteId, companyId);
        if (found.isEmpty()) {
            return EInvoicePushResult.localError(404,
                    "Credit note " + creditNoteId + " was not found for company " + companyId);
        }
        SaleCreditEInvoiceSnapshotLoader.Loaded loaded = found.get();
        EInvoiceSnapshot snapshot = loaded.snapshot();
        EInvoiceSnapshot.Header header = snapshot.header();
        String label = label(header);

        if (header.alreadySubmitted() && !mayResubmit(header)) {
            return alreadySubmitted(header, creditNoteId, companyId);
        }

        List<EInvoiceProblem> problems = validator.validate(snapshot, label);
        if (!problems.isEmpty()) {
            log.info("{} refused by e-invoice validation: {}", label,
                    problems.stream().map(EInvoiceProblem::code).toList());
            return validationFailed(label, problems);
        }

        Instant issuedAt = clock.instant();
        UblDocument document = builder.build(snapshot, issuedAt,
                EInvoiceDocumentKind.creditNote(loaded.invoiceNo(), loaded.invoiceUuid()));
        MyInvoisDocumentCodec.EncodedDocument encoded = codec.encode(document, header.invoiceNo());

        MyInvoisCall<DocumentSubmissionResponse> call = gateway.submit(encoded.toSubmissionRequest(), companyId);
        if (!call.success()) {
            if (call.result().success()) {
                // LHDN answered 2xx but the body could not be read: the document
                // may well be accepted. A blind retry would submit it twice.
                log.error("{}: LHDN answered HTTP {} but the reply could not be read: {}",
                        label, call.result().status(), call.result().body());
                return EInvoicePushResult.transportFailed("LHDN accepted the request for " + label
                        + " but its reply could not be read; check the LHDN portal for it before pushing again");
            }
            log.warn("{} submission failed: {}", label, call.message());
            return EInvoicePushResult.transportFailed(call.message());
        }

        DocumentSubmissionResponse response = call.data();
        if (response.getRejectedDocuments() != null && !response.getRejectedDocuments().isEmpty()) {
            String reason = MyInvoisErrors.describeRejection(response.getRejectedDocuments().get(0).getError());
            log.warn("{} rejected by LHDN:\n{}\nDocument was: {}", label, reason, encoded.json());
            return EInvoicePushResult.rejected(reason);
        }
        if (response.getAcceptedDocuments() == null || response.getAcceptedDocuments().isEmpty()
                || EInvoiceStatus.isBlank(response.getSubmissionUid())) {
            log.error("{}: LHDN answered 2xx but named no accepted document: {}", label, call.result().body());
            return EInvoicePushResult.transportFailed(
                    "LHDN accepted the request but did not return a document UUID; check the LHDN portal before retrying");
        }

        String uuid = response.getAcceptedDocuments().get(0).getUuid();
        String submissionUid = response.getSubmissionUid();
        // Logged BEFORE the save: if the save fails this line is the only record.
        log.info("{} accepted by LHDN: uuid={} submission={}", label, uuid, submissionUid);

        LocalDateTime pushedAt = LocalDateTime.ofInstant(issuedAt, EInvoiceStatus.MALAYSIA);
        try {
            creditNotes.claimEInvoiceSubmission(creditNoteId, companyId, uuid, submissionUid,
                    EInvoiceStatus.SUBMITTED, pushedAt);
        } catch (DataAccessException dbDown) {
            // The government has the document; our row does not know it. The
            // one thing that must not happen now is a second submission.
            log.error("{} WAS accepted by LHDN (uuid={}, submission={}) but the UUID could not be saved",
                    label, uuid, submissionUid, dbDown);
            return EInvoicePushResult.builder()
                    .outcome(EInvoicePushResult.Outcome.SUBMITTED)
                    .uuid(uuid)
                    .submissionUid(submissionUid)
                    .status(EInvoiceStatus.SUBMITTED)
                    .message(label + " WAS accepted by LHDN (UUID " + uuid
                            + ") but could not be recorded locally — do NOT push it again; "
                            + "record the UUID on the credit note and contact support")
                    .build();
        }

        ValidationOutcome outcome = readAndRecordStatus(creditNoteId, companyId, submissionUid, label);

        return EInvoicePushResult.builder()
                .outcome(EInvoicePushResult.Outcome.SUBMITTED)
                .uuid(uuid)
                .submissionUid(submissionUid)
                .longId(outcome.longId())
                .status(outcome.status())
                .shareUrl(shareUrl(uuid, outcome.longId()))
                .qrPngBase64(qrFor(uuid, outcome.longId()))
                .message(label + " submitted to LHDN"
                        + (EInvoiceStatus.isBlank(outcome.status()) ? "" : " (" + outcome.status() + ")"))
                .build();
    }

    // ───────────────────────────────────────────────────────────── status ──

    /**
     * Re-reads the LHDN status of an already-submitted credit note and records
     * whatever is now known. Legacy did this from the print path, so printing a
     * note polled the government as a side effect; here it is its own call.
     */
    public EInvoicePushResult refreshStatus(Integer creditNoteId, Integer companyId) {
        EInvoicePushResult notReady = precondition();
        if (notReady != null) {
            return notReady;
        }
        Optional<SaleCreditEInvoiceSnapshotLoader.Loaded> found = loader.load(creditNoteId, companyId);
        if (found.isEmpty()) {
            return EInvoicePushResult.localError(404,
                    "Credit note " + creditNoteId + " was not found for company " + companyId);
        }
        EInvoiceSnapshot.Header header = found.get().snapshot().header();
        String label = label(header);
        if (!header.alreadySubmitted()) {
            return EInvoicePushResult.localError(409, label + " has not been submitted to LHDN");
        }
        if (EInvoiceStatus.isBlank(header.eInvoiceSubmissionUid())) {
            return EInvoicePushResult.localError(409,
                    label + " has a document UUID but no submission id; its status cannot be read");
        }

        ValidationOutcome outcome;
        if (EInvoiceStatus.isFinal(header.eInvoiceStatus(), header.eInvoiceLongId())) {
            // Nothing more will change at LHDN; answer from what is stored.
            outcome = new ValidationOutcome(header.eInvoiceStatus(),
                    EInvoiceStatus.isBlank(header.eInvoiceLongId()) ? null : header.eInvoiceLongId(), null);
        } else {
            outcome = readAndRecordStatus(creditNoteId, companyId, header.eInvoiceSubmissionUid(), label);
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
                .message(label + " is "
                        + (EInvoiceStatus.isBlank(outcome.status()) ? "awaiting validation" : outcome.status()))
                .build();
    }

    /**
     * One GET of the submission. Records exactly what LHDN said: a status alone
     * while validation is pending; the long id and/or validated time once
     * either exists. On failure records nothing and says why.
     */
    private ValidationOutcome readAndRecordStatus(Integer creditNoteId, Integer companyId,
                                                  String submissionUid, String label) {
        MyInvoisCall<SubmissionStatusResponse> call = gateway.submissionStatus(submissionUid, companyId);
        if (!call.success()) {
            log.warn("{}: status read failed: {}", label, call.message());
            return ValidationOutcome.failed(call.message());
        }
        List<SubmissionStatusResponse.DocumentSummary> summaries = call.data().getDocumentSummary();
        if (summaries == null || summaries.isEmpty()) {
            // LHDN has not finished validating; the document is still Submitted.
            creditNotes.recordEInvoiceStatus(creditNoteId, companyId, EInvoiceStatus.SUBMITTED);
            return new ValidationOutcome(EInvoiceStatus.SUBMITTED, null, null);
        }

        SubmissionStatusResponse.DocumentSummary summary = summaries.get(0);
        String status = EInvoiceStatus.normalise(summary.getStatus());
        String longId = EInvoiceStatus.isBlank(summary.getLongId()) ? null : summary.getLongId().trim();
        LocalDateTime validatedAt = EInvoiceStatus.parseInstant(summary.getDateTimeValidated());
        if (validatedAt == null && !EInvoiceStatus.isBlank(summary.getDateTimeValidated())) {
            log.warn("{}: LHDN validated time '{}' could not be parsed; stored without it",
                    label, summary.getDateTimeValidated());
        }

        if (longId != null || validatedAt != null) {
            creditNotes.recordEInvoiceValidation(creditNoteId, companyId, longId == null ? "" : longId,
                    EInvoiceStatus.isBlank(status) ? EInvoiceStatus.SUBMITTED : status, validatedAt);
        } else if (!EInvoiceStatus.isBlank(status)) {
            creditNotes.recordEInvoiceStatus(creditNoteId, companyId, status);
        }
        if (EInvoiceStatus.INVALID.equalsIgnoreCase(status)) {
            log.warn("{} was validated as INVALID by LHDN: {}", label, summary.getDocumentStatusReason());
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
            return EInvoicePushResult.localError(409,
                    "MyInvois is not configured correctly on this server: " + misconfigured.getMessage());
        }
        return null;
    }

    /**
     * The answer for a credit note that already has a UUID. If LHDN's word on
     * it is not final, the status is read now, so a second click fetches the QR
     * exactly as it did in legacy.
     */
    private EInvoicePushResult alreadySubmitted(EInvoiceSnapshot.Header header, Integer creditNoteId, Integer companyId) {
        String label = label(header);
        String status = header.eInvoiceStatus();
        String longId = EInvoiceStatus.isBlank(header.eInvoiceLongId()) ? null : header.eInvoiceLongId();
        String readFailure = null;

        if (!EInvoiceStatus.isFinal(status, longId) && !EInvoiceStatus.isBlank(header.eInvoiceSubmissionUid())) {
            ValidationOutcome refreshed = readAndRecordStatus(creditNoteId, companyId,
                    header.eInvoiceSubmissionUid(), label);
            if (refreshed.failure() == null) {
                status = refreshed.status();
                longId = refreshed.longId();
            } else {
                readFailure = refreshed.failure();
            }
        }

        return EInvoicePushResult.builder()
                .outcome(EInvoicePushResult.Outcome.ALREADY_SUBMITTED)
                .uuid(header.eInvoiceUid())
                .submissionUid(header.eInvoiceSubmissionUid())
                .longId(longId)
                .status(status)
                .shareUrl(shareUrl(header.eInvoiceUid(), longId))
                .qrPngBase64(qrFor(header.eInvoiceUid(), longId))
                .message(label + " was already submitted to LHDN"
                        + (EInvoiceStatus.isBlank(status) ? " and is awaiting validation" : " (" + status + ")")
                        + (readFailure == null ? "" : "; its current status could not be read: " + readFailure))
                .build();
    }

    private boolean mayResubmit(EInvoiceSnapshot.Header header) {
        return properties.isAllowResubmitInvalid()
                && EInvoiceStatus.INVALID.equalsIgnoreCase(header.eInvoiceStatus());
    }

    private static EInvoicePushResult validationFailed(String label, List<EInvoiceProblem> problems) {
        List<String> details = problems.stream().map(EInvoiceProblem::message).toList();
        return EInvoicePushResult.builder()
                .outcome(EInvoicePushResult.Outcome.VALIDATION_FAILED)
                .message(label + " cannot be e-invoiced yet: " + details.size()
                        + (details.size() == 1 ? " problem" : " problems") + " to fix first")
                .details(details)
                .build();
    }

    /** How the credit note is named in every message the operator sees. */
    private static String label(EInvoiceSnapshot.Header header) {
        return "Credit note " + header.invoiceNo();
    }

    private String shareUrl(String uuid, String longId) {
        return EInvoiceStatus.isBlank(uuid) || EInvoiceStatus.isBlank(longId)
                ? null : urls.documentShareLink(uuid, longId);
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

    /** What one status read established. */
    private record ValidationOutcome(String status, String longId, LocalDateTime validatedAt, String failure) {

        ValidationOutcome(String status, String longId, LocalDateTime validatedAt) {
            this(status, longId, validatedAt, null);
        }

        static ValidationOutcome failed(String failure) {
            return new ValidationOutcome(null, null, null, failure);
        }
    }
}
