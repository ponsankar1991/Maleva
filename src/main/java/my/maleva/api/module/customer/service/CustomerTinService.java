package my.maleva.api.module.customer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.integration.myinvois.MyInvoisCall;
import my.maleva.api.integration.myinvois.MyInvoisGateway;
import my.maleva.api.integration.myinvois.MyInvoisResult;
import my.maleva.api.integration.myinvois.TaxpayerIdType;
import my.maleva.api.integration.myinvois.dto.TaxpayerTinResponse;
import my.maleva.api.module.customer.dto.CustomerTinCheckRequest;
import my.maleva.api.module.customer.dto.CustomerTinCheckResult;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * The customer screen's TIN button, the port of legacy
 * {@code CustomerServices.CheckCustomerTin}.
 *
 * <p>It is two LHDN operations behind one button, chosen by whether a TIN is
 * already on the form:
 *
 * <ul>
 *   <li><b>TIN filled → validate.</b> "Does this TIN exist, and does it belong
 *       to this name and registration number?" LHDN answers with a status, no
 *       body, so a success means the pairing is good and the form keeps the TIN
 *       it already had.</li>
 *   <li><b>TIN blank → search.</b> "What is the TIN for this name and
 *       registration number?" The answer is written into the field.</li>
 * </ul>
 *
 * <p>Legacy reached the first URL by string-replacing {@code "tin?"} inside the
 * second, which made them look like one endpoint with a flag. They are two
 * endpoints and are called as such.
 *
 * <p>Two legacy behaviours are deliberately not copied. Its query string was
 * concatenated raw, so a taxpayer name containing {@code &} or a space
 * truncated or corrupted the request — the parameters are URL-encoded here.
 * And it reported a failure with {@code StatusCode = Success}, which is what a
 * caller reading the status rather than the flag would have seen as a pass.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerTinService {

    private final MyInvoisGateway gateway;

    public CustomerTinCheckResult check(CustomerTinCheckRequest request, Integer companyId) {
        String query = buildQuery(request);
        String tin = trimToNull(request.getCustomerTin());

        if (tin != null) {
            MyInvoisResult result = gateway.validateTin(tin, query, companyId);
            if (!result.success()) {
                log.warn("TIN validation refused for {}: {}", tin, result.message());
                return CustomerTinCheckResult.invalid(result.message());
            }
            // Nothing to read back: the TIN on the form is the one that was
            // confirmed, and legacy left the field untouched here too.
            return CustomerTinCheckResult.valid(tin, "TIN is valid for this taxpayer");
        }

        MyInvoisCall<TaxpayerTinResponse> call = gateway.searchTin(query, companyId);
        if (!call.success()) {
            log.warn("TIN search refused: {}", call.message());
            return CustomerTinCheckResult.invalid(call.message());
        }

        String found = trimToNull(call.data().getTin());
        if (found == null) {
            // LHDN answered, but knows no TIN for this taxpayer. Legacy showed
            // "Valid Tin No" here — the same message it used for success — so
            // an operator could not tell the two apart.
            return CustomerTinCheckResult.notFound(
                    "LHDN has no TIN registered for this name and registration number");
        }
        return CustomerTinCheckResult.valid(found, "TIN found for this taxpayer");
    }

    /**
     * {@code ?taxpayerName=…&idValue=…&idType=…}, in the order legacy built it,
     * skipping anything blank. Empty when nothing is filled in, which is a
     * bare lookup LHDN will refuse — the same as before.
     *
     * <p>{@code idType} only travels with {@code idValue}, because it describes
     * it: an idValue with no idType is rejected by LHDN.
     */
    private String buildQuery(CustomerTinCheckRequest request) {
        List<String> parts = new ArrayList<>();

        String name = trimToNull(request.getTaxpayerName());
        if (name != null) {
            parts.add("taxpayerName=" + encode(name));
        }

        String idValue = trimToNull(request.getIdValue());
        if (idValue != null) {
            parts.add("idValue=" + encode(idValue));
            parts.add("idType=" + TaxpayerIdType.of(idValue).name());
        }

        String fileType = trimToNull(request.getFileType());
        if (fileType != null) {
            parts.add("fileType=" + encode(fileType));
        }

        return parts.isEmpty() ? "" : "?" + String.join("&", parts);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
