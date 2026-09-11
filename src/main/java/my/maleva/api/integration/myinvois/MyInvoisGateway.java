package my.maleva.api.integration.myinvois;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.integration.myinvois.dto.DocumentSubmissionRequest;
import my.maleva.api.integration.myinvois.dto.DocumentSubmissionResponse;
import my.maleva.api.integration.myinvois.dto.SubmissionStatusResponse;
import my.maleva.api.integration.myinvois.dto.TaxpayerTinResponse;
import org.springframework.stereotype.Component;

/**
 * The typed MyInvois operations the modules call. Each method is one LHDN
 * endpoint; the client handles auth and transport, this class handles URLs
 * and parsing. Never throws.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MyInvoisGateway {

    private final MyInvoisClient client;
    private final MyInvoisUrls urls;
    private final ObjectMapper objectMapper;

    /**
     * {@code POST /documentsubmissions}. A 202 means LHDN has queued the
     * documents for validation — it is not yet an accepted e-invoice.
     */
    public MyInvoisCall<DocumentSubmissionResponse> submit(DocumentSubmissionRequest request, Integer companyId) {
        MyInvoisResult result = client.post(urls.documentSubmissions(), request, companyId);
        return parse(result, DocumentSubmissionResponse.class);
    }

    /** {@code GET /documentsubmissions/{uid}}: the validation outcome so far. */
    public MyInvoisCall<SubmissionStatusResponse> submissionStatus(String submissionUid, Integer companyId) {
        MyInvoisResult result = client.get(urls.documentSubmission(submissionUid), companyId);
        return parse(result, SubmissionStatusResponse.class);
    }

    /**
     * {@code GET /taxpayer/validate/{tin}?…}: does this TIN exist, and does it
     * belong to the name and registration number given?
     *
     * <p>Returns the raw result rather than a {@link MyInvoisCall} because
     * there is nothing to parse — LHDN answers a valid pairing with a bare 200
     * and an invalid one with an error status. A call whose body is empty
     * would never satisfy {@code MyInvoisCall.success()}.
     */
    public MyInvoisResult validateTin(String tin, String query, Integer companyId) {
        return client.get(urls.validateTin(tin, query), companyId);
    }

    /** {@code GET /taxpayer/search/tin?…}: the TIN for a name and registration number. */
    public MyInvoisCall<TaxpayerTinResponse> searchTin(String query, Integer companyId) {
        return parse(client.get(urls.searchTin(query), companyId), TaxpayerTinResponse.class);
    }

    private <T> MyInvoisCall<T> parse(MyInvoisResult result, Class<T> type) {
        if (!result.success()) {
            return MyInvoisCall.failed(result);
        }
        try {
            T data = objectMapper.readValue(result.body(), type);
            if (data == null) {
                return MyInvoisCall.failed(MyInvoisResult.failed(result.status(), result.body(),
                        "LHDN returned an empty response"));
            }
            return new MyInvoisCall<>(result, data);
        } catch (Exception ex) {
            log.error("MyInvois response could not be parsed as {}: {}", type.getSimpleName(),
                    MyInvoisErrors.abbreviate(result.body(), 1000), ex);
            return MyInvoisCall.failed(MyInvoisResult.failed(result.status(), result.body(),
                    "LHDN response could not be read: " + ex.getMessage()));
        }
    }
}
