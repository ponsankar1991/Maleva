package my.maleva.api.module.invoice.einvoice;

import my.maleva.api.common.config.MyInvoisProperties;
import my.maleva.api.integration.myinvois.MyInvoisCall;
import my.maleva.api.integration.myinvois.MyInvoisDocumentCodec;
import my.maleva.api.integration.myinvois.MyInvoisGateway;
import my.maleva.api.integration.myinvois.MyInvoisQrCode;
import my.maleva.api.integration.myinvois.MyInvoisResult;
import my.maleva.api.integration.myinvois.MyInvoisUrls;
import my.maleva.api.integration.myinvois.dto.DocumentSubmissionRequest;
import my.maleva.api.integration.myinvois.dto.DocumentSubmissionResponse;
import my.maleva.api.integration.myinvois.dto.ErrorResponse;
import my.maleva.api.integration.myinvois.dto.SubmissionStatusResponse;
import my.maleva.api.module.invoice.repository.SaleMasterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The orchestration, with the wire mocked. The one assertion that matters
 * most: the UUID is saved BEFORE the status read, so a failure there can no
 * longer cause a duplicate government document.
 */
class SaleInvoiceEInvoiceServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-05T01:30:00Z");

    private MyInvoisProperties properties;
    private EInvoiceSnapshotLoader loader;
    private MyInvoisGateway gateway;
    private SaleMasterRepository saleMasters;
    private SaleInvoiceEInvoiceService service;

    @BeforeEach
    void setUp() {
        properties = EInvoiceFixtures.properties();
        loader = Mockito.mock(EInvoiceSnapshotLoader.class);
        gateway = Mockito.mock(MyInvoisGateway.class);
        saleMasters = Mockito.mock(SaleMasterRepository.class);

        service = new SaleInvoiceEInvoiceService(
                properties, loader, new EInvoiceValidator(properties), new EInvoiceDocumentBuilder(properties),
                new MyInvoisDocumentCodec(), gateway, new MyInvoisUrls(properties), new MyInvoisQrCode(),
                saleMasters, Clock.fixed(NOW, ZoneId.of("Asia/Kuala_Lumpur")));

        when(loader.load(4711, 1)).thenReturn(Optional.of(EInvoiceFixtures.snapshot()));
    }

    @Test
    void acceptedSubmissionSavesTheUuidBeforeReadingStatus() {
        when(gateway.submit(any(), eq(1))).thenReturn(accepted("UUID-1", "SUB-1"));
        when(gateway.submissionStatus("SUB-1", 1)).thenReturn(statusPending());

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.SUBMITTED);
        assertThat(result.uuid()).isEqualTo("UUID-1");
        assertThat(result.submissionUid()).isEqualTo("SUB-1");
        assertThat(result.status()).isEqualTo("Submitted");
        assertThat(result.shareUrl()).isNull(); // not validated yet, no long id

        InOrder order = inOrder(saleMasters, gateway);
        order.verify(gateway).submit(any(), eq(1));
        order.verify(saleMasters).claimEInvoiceSubmission(eq(4711), eq(1), eq("UUID-1"), eq("SUB-1"),
                eq("Submitted"), eq(LocalDateTime.of(2026, 9, 5, 9, 30)));
        order.verify(gateway).submissionStatus("SUB-1", 1);
        verify(saleMasters, never()).recordEInvoiceValidation(anyInt(), anyInt(), anyString(), anyString(), any());
    }

    @Test
    void validatedSubmissionRecordsLongIdStatusAndValidatedTimeAndReturnsQr() {
        when(gateway.submit(any(), eq(1))).thenReturn(accepted("UUID-1", "SUB-1"));
        when(gateway.submissionStatus("SUB-1", 1)).thenReturn(statusValid("LONG-1", "2026-09-05T01:30:05Z"));

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.status()).isEqualTo("Valid");
        assertThat(result.longId()).isEqualTo("LONG-1");
        assertThat(result.shareUrl()).isEqualTo("https://preprod.myinvois.hasil.gov.my/UUID-1/share/LONG-1");
        assertThat(result.qrPngBase64()).isNotBlank();
        verify(saleMasters).recordEInvoiceValidation(4711, 1, "LONG-1", "Valid", LocalDateTime.of(2026, 9, 5, 9, 30, 5));
    }

    @Test
    void submittedDocumentIsTheValidatedSnapshotEncodedOnce() {
        when(gateway.submit(any(), eq(1))).thenReturn(accepted("UUID-1", "SUB-1"));
        when(gateway.submissionStatus(anyString(), eq(1))).thenReturn(statusPending());

        service.push(4711, 1);

        ArgumentCaptor<DocumentSubmissionRequest> request = ArgumentCaptor.forClass(DocumentSubmissionRequest.class);
        verify(gateway).submit(request.capture(), eq(1));
        DocumentSubmissionRequest.Document doc = request.getValue().getDocuments().get(0);
        assertThat(doc.getCodeNumber()).isEqualTo("INV000004711");
        assertThat(doc.getFormat()).isEqualTo("JSON");
        String json = new String(java.util.Base64.getDecoder().decode(doc.getDocument()));
        assertThat(json).contains("\"ID\":[{\"_\":\"INV000004711\"}]").contains("\"_\":262.00,\"currencyID\":\"MYR\"");
    }

    @Test
    void rejectedDocumentSavesNothingAndReportsTheReason() {
        DocumentSubmissionResponse response = DocumentSubmissionResponse.builder()
                .submissionUid("SUB-1")
                .rejectedDocuments(List.of(DocumentSubmissionResponse.RejectedDocument.builder()
                        .invoiceCodeNumber("INV000004711")
                        .error(ErrorResponse.ErrorDetail.builder().code("CF321").message("Bad TIN").build())
                        .build()))
                .build();
        when(gateway.submit(any(), eq(1))).thenReturn(new MyInvoisCall<>(MyInvoisResult.ok(202, "{}"), response));

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.REJECTED);
        assertThat(result.message()).contains("Error Code: CF321").contains("Bad TIN");
        verifyNoInteractions(saleMasters);
    }

    @Test
    void transportFailureSavesNothing() {
        when(gateway.submit(any(), eq(1))).thenReturn(MyInvoisCall.failed(MyInvoisResult.notSent("Could not reach LHDN")));

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.TRANSPORT_FAILED);
        assertThat(result.message()).contains("Could not reach LHDN");
        verifyNoInteractions(saleMasters);
    }

    @Test
    void validationFailureNeverTouchesTheWire() {
        EInvoiceSnapshot broken = EInvoiceFixtures.snapshot().toBuilder()
                .customer(EInvoiceFixtures.customer().toBuilder().tin("").build()).build();
        when(loader.load(4711, 1)).thenReturn(Optional.of(broken));

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.VALIDATION_FAILED);
        assertThat(result.details()).hasSize(1);
        assertThat(result.details().get(0)).contains("has no TIN");
        assertThat(result.message()).contains("1 problem to fix");
        verifyNoInteractions(gateway, saleMasters);
    }

    @Test
    void alreadyValidatedInvoiceIsNotSentAgainAndNeedsNoLhdnCall() {
        EInvoiceSnapshot pushed = withEInvoice("UUID-OLD", "SUB-OLD", "LONG-OLD", "Valid");
        when(loader.load(4711, 1)).thenReturn(Optional.of(pushed));

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.ALREADY_SUBMITTED);
        assertThat(result.success()).isTrue();
        assertThat(result.shareUrl()).endsWith("/UUID-OLD/share/LONG-OLD");
        verifyNoInteractions(gateway, saleMasters);
    }

    @Test
    void pushOnAnAwaitingInvoiceRefreshesItsStatusLikeTheLegacyButton() {
        // Legacy: clicking e-invoice again on a pushed invoice re-read LHDN and produced the QR.
        when(loader.load(4711, 1)).thenReturn(Optional.of(withEInvoice("UUID-1", "SUB-1", "", "")));
        when(gateway.submissionStatus("SUB-1", 1)).thenReturn(statusValid("LONG-7", "2026-09-05T01:40:00Z"));

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.ALREADY_SUBMITTED);
        assertThat(result.status()).isEqualTo("Valid");
        assertThat(result.longId()).isEqualTo("LONG-7");
        assertThat(result.qrPngBase64()).isNotBlank();
        verify(gateway, never()).submit(any(), anyInt());
        verify(saleMasters).recordEInvoiceValidation(4711, 1, "LONG-7", "Valid", LocalDateTime.of(2026, 9, 5, 9, 40));
    }

    @Test
    void invalidDocumentMayBeResubmittedWhenAllowed() {
        when(loader.load(4711, 1)).thenReturn(Optional.of(withEInvoice("UUID-OLD", "SUB-OLD", "", "Invalid")));
        when(gateway.submit(any(), eq(1))).thenReturn(accepted("UUID-2", "SUB-2"));
        when(gateway.submissionStatus("SUB-2", 1)).thenReturn(statusPending());

        properties.setAllowResubmitInvalid(false);
        assertThat(service.push(4711, 1).outcome()).isEqualTo(EInvoicePushResult.Outcome.ALREADY_SUBMITTED);

        properties.setAllowResubmitInvalid(true);
        EInvoicePushResult result = service.push(4711, 1);
        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.SUBMITTED);
        assertThat(result.uuid()).isEqualTo("UUID-2");
    }

    @Test
    void refreshStatusRecordsValidationWhenItArrives() {
        when(loader.load(4711, 1)).thenReturn(Optional.of(withEInvoice("UUID-1", "SUB-1", "", "Submitted")));
        when(gateway.submissionStatus("SUB-1", 1)).thenReturn(statusValid("LONG-9", "2026-09-05T02:00:00Z"));

        EInvoicePushResult result = service.refreshStatus(4711, 1);

        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.STATUS_REFRESHED);
        assertThat(result.status()).isEqualTo("Valid");
        assertThat(result.shareUrl()).endsWith("/UUID-1/share/LONG-9");
        verify(saleMasters).recordEInvoiceValidation(4711, 1, "LONG-9", "Valid", LocalDateTime.of(2026, 9, 5, 10, 0));
    }

    @Test
    void statusReadFailureAfterAcceptanceStillReportsSubmittedWithTheUuidSaved() {
        when(gateway.submit(any(), eq(1))).thenReturn(accepted("UUID-1", "SUB-1"));
        when(gateway.submissionStatus("SUB-1", 1))
                .thenReturn(MyInvoisCall.failed(MyInvoisResult.failed(429, "", "rate-limited")));

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.SUBMITTED);
        assertThat(result.uuid()).isEqualTo("UUID-1");
        assertThat(result.status()).isNull();
        verify(saleMasters).claimEInvoiceSubmission(eq(4711), eq(1), eq("UUID-1"), eq("SUB-1"), eq("Submitted"), any());
        verify(saleMasters, never()).recordEInvoiceValidation(anyInt(), anyInt(), anyString(), anyString(), any());
    }

    @Test
    void databaseFailureAfterAcceptanceWarnsNotToPushAgain() {
        when(gateway.submit(any(), eq(1))).thenReturn(accepted("UUID-1", "SUB-1"));
        Mockito.doThrow(new org.springframework.dao.DataAccessResourceFailureException("db down"))
                .when(saleMasters).claimEInvoiceSubmission(anyInt(), anyInt(), anyString(), anyString(), anyString(), any());

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.SUBMITTED);
        assertThat(result.message()).contains("UUID-1").contains("do NOT push it again");
        verify(gateway, never()).submissionStatus(anyString(), anyInt());
    }

    @Test
    void invalidDocumentIsNotRepolledWhenResubmissionIsOff() {
        properties.setAllowResubmitInvalid(false);
        when(loader.load(4711, 1)).thenReturn(Optional.of(withEInvoice("UUID-OLD", "SUB-OLD", "", "Invalid")));

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.outcome()).isEqualTo(EInvoicePushResult.Outcome.ALREADY_SUBMITTED);
        assertThat(result.lhdnInvalid()).isTrue();
        verifyNoInteractions(gateway, saleMasters);
    }

    @Test
    void validWithoutALongIdIsReadAgainUntilTheLongIdArrives() {
        when(loader.load(4711, 1)).thenReturn(Optional.of(withEInvoice("UUID-1", "SUB-1", "", "Valid")));
        when(gateway.submissionStatus("SUB-1", 1)).thenReturn(statusValid("LONG-1", "2026-09-05T01:40:00Z"));

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.longId()).isEqualTo("LONG-1");
        verify(saleMasters).recordEInvoiceValidation(eq(4711), eq(1), eq("LONG-1"), eq("Valid"), any());
    }

    @Test
    void invalidVerdictRecordsItsValidatedTimeWithoutALongId() {
        when(gateway.submit(any(), eq(1))).thenReturn(accepted("UUID-1", "SUB-1"));
        SubmissionStatusResponse invalid = SubmissionStatusResponse.builder()
                .overallStatus("invalid")
                .documentSummary(List.of(SubmissionStatusResponse.DocumentSummary.builder()
                        .uuid("UUID-1").longId("").status("Invalid").dateTimeValidated("2026-09-05T01:31:00Z")
                        .documentStatusReason("TIN mismatch").build()))
                .build();
        when(gateway.submissionStatus("SUB-1", 1)).thenReturn(new MyInvoisCall<>(MyInvoisResult.ok(200, "{}"), invalid));

        EInvoicePushResult result = service.push(4711, 1);

        assertThat(result.status()).isEqualTo("Invalid");
        assertThat(result.shareUrl()).isNull();
        verify(saleMasters).recordEInvoiceValidation(4711, 1, "", "Invalid", LocalDateTime.of(2026, 9, 5, 9, 31));
    }

    @Test
    void lhdnTimestampsWithoutAZoneAreTakenAsUtc() {
        assertThat(SaleInvoiceEInvoiceService.parseLhdnInstant("2026-09-05T01:40:00")).isEqualTo(LocalDateTime.of(2026, 9, 5, 9, 40));
        assertThat(SaleInvoiceEInvoiceService.parseLhdnInstant("2026-09-05T01:40:00+08:00")).isEqualTo(LocalDateTime.of(2026, 9, 5, 1, 40));
        assertThat(SaleInvoiceEInvoiceService.parseLhdnInstant("garbage")).isNull();
        assertThat(SaleInvoiceEInvoiceService.parseLhdnInstant(null)).isNull();
    }

    @Test
    void refreshStatusOfAValidatedInvoiceAnswersFromStorage() {
        when(loader.load(4711, 1)).thenReturn(Optional.of(withEInvoice("UUID-1", "SUB-1", "LONG-1", "Valid")));

        EInvoicePushResult result = service.refreshStatus(4711, 1);

        assertThat(result.status()).isEqualTo("Valid");
        verifyNoInteractions(gateway, saleMasters);
    }

    @Test
    void unknownInvoiceIs404AndDisabledIntegrationIs409() {
        when(loader.load(99, 1)).thenReturn(Optional.empty());
        assertThat(service.push(99, 1).errorStatus()).isEqualTo(404);

        properties.setEnabled(false);
        assertThat(service.push(4711, 1).errorStatus()).isEqualTo(409);
    }

    // ────────────────────────────────────────────────────────────── helpers ──

    private static MyInvoisCall<DocumentSubmissionResponse> accepted(String uuid, String submissionUid) {
        DocumentSubmissionResponse response = DocumentSubmissionResponse.builder()
                .submissionUid(submissionUid)
                .acceptedDocuments(List.of(DocumentSubmissionResponse.AcceptedDocument.builder()
                        .uuid(uuid).invoiceCodeNumber("INV000004711").build()))
                .build();
        return new MyInvoisCall<>(MyInvoisResult.ok(202, "{}"), response);
    }

    private static MyInvoisCall<SubmissionStatusResponse> statusPending() {
        SubmissionStatusResponse response = SubmissionStatusResponse.builder()
                .overallStatus("in progress").documentSummary(List.of()).build();
        return new MyInvoisCall<>(MyInvoisResult.ok(200, "{}"), response);
    }

    private static MyInvoisCall<SubmissionStatusResponse> statusValid(String longId, String validatedAt) {
        SubmissionStatusResponse response = SubmissionStatusResponse.builder()
                .overallStatus("valid")
                .documentSummary(List.of(SubmissionStatusResponse.DocumentSummary.builder()
                        .uuid("UUID-1").longId(longId).status("Valid").dateTimeValidated(validatedAt).build()))
                .build();
        return new MyInvoisCall<>(MyInvoisResult.ok(200, "{}"), response);
    }

    private static EInvoiceSnapshot withEInvoice(String uuid, String submissionUid, String longId, String status) {
        return EInvoiceFixtures.snapshot().toBuilder()
                .header(EInvoiceFixtures.header().toBuilder()
                        .eInvoiceUid(uuid).eInvoiceSubmissionUid(submissionUid)
                        .eInvoiceLongId(longId).eInvoiceStatus(status).build())
                .build();
    }
}
