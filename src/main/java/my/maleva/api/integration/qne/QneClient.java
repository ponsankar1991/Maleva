package my.maleva.api.integration.qne;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.QneProperties;
import my.maleva.api.integration.qne.dto.QneErrorMsg;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Transport to the QNE cloud accounting system.
 *
 * <p>Faithful port of the legacy {@code commonfunctions.QneApi}. The details
 * below look arbitrary but each one is a behaviour the legacy call sites are
 * written against, so they are contract, not style:
 *
 * <ul>
 *   <li><b>{@code DbCode} header on every request</b> — QNE routes to the
 *       tenant database by this header. It is chosen by the {@code qne.demo}
 *       flag: trial database when true, live when false. The legacy bug class
 *       this replaces was a header sent empty because the config key did not
 *       exist; an empty DbCode makes QNE answer for the wrong tenant or 401.</li>
 *   <li><b>30-minute read timeout</b> — QNE report generation and bulk posts
 *       genuinely run for minutes. The legacy client set exactly this.</li>
 *   <li><b>400 and 404 pass the body through</b> — QNE puts validation detail
 *       (duplicate code, missing account) in those bodies, and the legacy UI
 *       shows them verbatim. Wrapping them in a generic error hides the one
 *       message the accounts clerk needs.</li>
 *   <li><b>Other HTTP errors parse {@code ErrorMsg}</b> — QNE's error envelope
 *       is {@code {code, Message}}, surfaced as {@code code\nMessage} exactly
 *       as the legacy did.</li>
 *   <li><b>Never throws</b> — every failure becomes an unsuccessful
 *       {@link QneResult}. Transport failures report the root cause, matching
 *       the legacy loop that unwound {@code InnerException}.</li>
 * </ul>
 *
 * <p>When {@code qne.enabled} is false the client refuses to call out and
 * reports that, mirroring the legacy {@code qneapilist.qneapi} gate — but in
 * one place instead of at every call site.
 */
@Slf4j
@Component
public class QneClient {

    /** Legacy: {@code client.Timeout = TimeSpan.FromMinutes(30)}. */
    static final Duration READ_TIMEOUT = Duration.ofMinutes(30);
    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);

    static final String DISABLED_MESSAGE =
            "QNE integration is disabled (qne.enabled=false)";

    private final QneProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    @Autowired
    public QneClient(QneProperties properties, ObjectMapper objectMapper) {
        this(properties, objectMapper, RestClient.builder().requestFactory(timeoutFactory()));
    }

    /**
     * Visible for tests, which bind a mock server onto the builder — the
     * builder is used as handed in, so the mock's request factory survives.
     */
    QneClient(QneProperties properties, ObjectMapper objectMapper, RestClient.Builder builder) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = builder.build();
    }

    private static SimpleClientHttpRequestFactory timeoutFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return requestFactory;
    }

    /**
     * The tenant database this client is talking to.
     *
     * <p>Resolved per call rather than cached so flipping {@code qne.demo}
     * does not need a restart — the legacy static field had the same
     * always-current behaviour within a deploy.
     */
    public String dbCode() {
        QneProperties.Db db = properties.getDb();
        return properties.isDemo() ? db.getTrial() : db.getLive();
    }

    public QneResult get(String url) {
        return exchange(HttpMethod.GET, url, null);
    }

    public QneResult post(String url, Object body) {
        return exchange(HttpMethod.POST, url, body);
    }

    public QneResult put(String url, Object body) {
        return exchange(HttpMethod.PUT, url, body);
    }

    private QneResult exchange(HttpMethod method, String url, Object body) {
        if (!properties.isEnabled()) {
            log.info("QNE call suppressed, integration disabled: {} {}", method, url);
            return QneResult.notSent(DISABLED_MESSAGE);
        }

        try {
            RestClient.RequestBodySpec request = restClient
                    .method(method)
                    .uri(url)
                    .header("DbCode", dbCode())
                    .accept(MediaType.APPLICATION_JSON);

            if (body != null) {
                // Serialised through the shared ObjectMapper, matching the
                // legacy JsonConvert.SerializeObject step, so date and casing
                // conventions stay consistent with the rest of the API.
                // FAIL_ON_EMPTY_BEANS is off because Json.NET wrote a
                // property-less object as {} and never threw; Jackson's
                // default would turn that into a transport failure.
                request = request
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writer()
                                .without(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                                .writeValueAsString(body));
            }

            // exchange() hands us every status without throwing, which is the
            // whole point: 400/404 carry messages the caller must see.
            return request.exchange((clientRequest, clientResponse) -> {
                int status = clientResponse.getStatusCode().value();
                String responseBody =
                        new String(clientResponse.getBody().readAllBytes(), StandardCharsets.UTF_8);
                return toResult(method, url, status, responseBody);
            });

        } catch (Exception ex) {
            Throwable root = rootCause(ex);
            log.error("QNE transport failure: {} {}", method, url, ex);
            return QneResult.notSent(root.toString());
        }
    }

    private QneResult toResult(HttpMethod method, String url, int status, String body) {
        if (status >= 200 && status < 300) {
            return QneResult.ok(status, body);
        }

        if (status == HttpStatus.NOT_FOUND.value() || status == HttpStatus.BAD_REQUEST.value()) {
            // Legacy contract: these two carry QNE's own validation text and
            // pass through untouched.
            log.warn("QNE rejected {} {} with {}: {}", method, url, status, body);
            return QneResult.failed(status, body, body);
        }

        log.error("QNE error on {} {}: HTTP {} {}", method, url, status, body);
        return QneResult.failed(status, body, describeError(body));
    }

    /**
     * Renders QNE's error envelope the way the legacy did:
     * {@code code\nMessage}. A body that is not the envelope — HTML from a
     * gateway, an empty string — falls back to itself, which the legacy
     * version did not survive (its deserialize threw and the real error was
     * replaced by a parse error).
     */
    private String describeError(String body) {
        try {
            QneErrorMsg error = objectMapper.readValue(body, QneErrorMsg.class);
            if (error != null && (error.getCode() != null || error.getMessage() != null)) {
                return (error.getCode() == null ? "" : error.getCode())
                        + "\n"
                        + (error.getMessage() == null ? "" : error.getMessage());
            }
        } catch (Exception ignored) {
            // Not the ErrorMsg envelope; the raw body is more useful than a
            // parse failure.
        }
        return body;
    }

    private static Throwable rootCause(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
