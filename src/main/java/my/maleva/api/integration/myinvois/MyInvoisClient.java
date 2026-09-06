package my.maleva.api.integration.myinvois;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.MyInvoisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Transport to LHDN MyInvois. Bearer-authenticated JSON over HTTPS; never throws.
 *
 * <p>Port of the legacy {@code commonfunctions.EInvoiceApi} with its two
 * transport defects fixed:
 * <ul>
 *   <li><b>401 handling.</b> Legacy detected an expired token by searching the
 *       response text for the word "Unauthorized" — an empty 401 body never
 *       matched — then refreshed the token and returned the failure anyway, so
 *       the operator had to click again. Here a 401 refreshes the token and
 *       retries the same request once.</li>
 *   <li><b>429 handling.</b> Legacy's rate-limit branch was commented out, so
 *       a 429 surfaced as an opaque failure. Here it is named, with LHDN's
 *       {@code Retry-After} when present. It is still not retried
 *       automatically: a submission must never be re-POSTed by the client,
 *       or one invoice can become two LHDN documents.</li>
 * </ul>
 *
 * <p>When {@code myinvois.enabled} is false the client refuses to call out and
 * says so, mirroring the legacy {@code EInvoiceeapi} gate in one place.
 */
@Slf4j
@Component
public class MyInvoisClient {

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);
    public static final String DISABLED_MESSAGE = "MyInvois e-invoicing is disabled (myinvois.enabled=false)";

    private final MyInvoisProperties properties;
    private final MyInvoisTokenProvider tokens;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public MyInvoisClient(MyInvoisProperties properties, MyInvoisTokenProvider tokens, ObjectMapper objectMapper) {
        this(properties, tokens, objectMapper, RestClient.builder().requestFactory(timeoutFactory(properties)));
    }

    /** Visible for tests, which bind a mock server onto the builder. */
    MyInvoisClient(MyInvoisProperties properties, MyInvoisTokenProvider tokens,
                   ObjectMapper objectMapper, RestClient.Builder builder) {
        this.properties = properties;
        this.tokens = tokens;
        this.objectMapper = objectMapper;
        this.restClient = builder.build();
    }

    private static SimpleClientHttpRequestFactory timeoutFactory(MyInvoisProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(properties.getTimeout());
        return factory;
    }

    public MyInvoisResult get(String url, Integer companyId) {
        return exchangeWithTokenRetry(HttpMethod.GET, url, null, companyId);
    }

    public MyInvoisResult post(String url, Object body, Integer companyId) {
        return exchangeWithTokenRetry(HttpMethod.POST, url, body, companyId);
    }

    // ───────────────────────────────────────────────────────────── internals ──

    private MyInvoisResult exchangeWithTokenRetry(HttpMethod method, String url, Object body, Integer companyId) {
        if (!properties.isEnabled()) {
            log.info("MyInvois call suppressed, integration disabled: {} {}", method, url);
            return MyInvoisResult.notSent(DISABLED_MESSAGE);
        }

        MyInvoisTokenProvider.TokenResult token = tokens.accessToken(companyId);
        if (!token.success()) {
            return MyInvoisResult.notSent(token.failure());
        }

        MyInvoisResult first = exchange(method, url, body, token.accessToken());
        if (!first.isUnauthorized()) {
            return first;
        }

        log.warn("MyInvois answered 401 on {} {}; refreshing the token and retrying once", method, url);
        MyInvoisTokenProvider.TokenResult refreshed = tokens.refresh(companyId, token.accessToken());
        if (!refreshed.success()) {
            return MyInvoisResult.failed(401, first.body(),
                    "LHDN rejected the access token and a new one could not be obtained: " + refreshed.failure());
        }
        return exchange(method, url, body, refreshed.accessToken());
    }

    private MyInvoisResult exchange(HttpMethod method, String url, Object body, String bearerToken) {
        try {
            RestClient.RequestBodySpec request = restClient
                    .method(method)
                    .uri(url)
                    .header("Authorization", "Bearer " + bearerToken)
                    .accept(MediaType.APPLICATION_JSON);

            if (body != null) {
                request = request
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(body));
            }

            // exchange() hands back every status without throwing; 4xx bodies
            // carry the validation text the operator must see.
            return request.exchange((clientRequest, clientResponse) -> {
                int status = clientResponse.getStatusCode().value();
                String responseBody =
                        new String(clientResponse.getBody().readAllBytes(), StandardCharsets.UTF_8);
                String retryAfter = clientResponse.getHeaders().getFirst("Retry-After");
                return toResult(method, url, status, responseBody, retryAfter);
            });

        } catch (Exception ex) {
            Throwable root = rootCause(ex);
            log.error("MyInvois transport failure: {} {}", method, url, ex);
            return MyInvoisResult.notSent("Could not reach LHDN MyInvois: " + root.getMessage());
        }
    }

    private MyInvoisResult toResult(HttpMethod method, String url, int status, String body, String retryAfter) {
        if (status >= 200 && status < 300) {
            return MyInvoisResult.ok(status, body);
        }

        if (status == 429) {
            String message = "LHDN is rate-limiting this taxpayer (HTTP 429)"
                    + (retryAfter == null ? "" : "; retry after " + retryAfter + " seconds");
            log.warn("MyInvois rate limit on {} {}: {}", method, url, message);
            return MyInvoisResult.failed(status, body, message);
        }

        String message = MyInvoisErrors.describeHttpError(objectMapper, status, body);
        if (status == 401) {
            log.warn("MyInvois unauthorised on {} {}", method, url);
        } else {
            // The raw body stays in the log; the operator gets the rendered text.
            log.error("MyInvois error on {} {}: HTTP {} {}", method, url, status,
                    MyInvoisErrors.abbreviate(body == null ? "" : body, 2000));
        }
        return MyInvoisResult.failed(status, body, message);
    }

    private static Throwable rootCause(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
