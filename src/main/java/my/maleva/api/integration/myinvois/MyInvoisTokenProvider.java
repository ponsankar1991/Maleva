package my.maleva.api.integration.myinvois;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.MyInvoisProperties;
import my.maleva.api.integration.myinvois.dto.TokenResponse;
import my.maleva.api.module.company.entity.CompanySettings;
import my.maleva.api.module.company.repository.CompanySettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Obtains and caches the MyInvois bearer token (OAuth2 client credentials).
 *
 * <p>Same lifecycle as the legacy {@code AuthenticateCall}: a token is kept in
 * memory, mirrored to {@code CompanySettings} so a restart does not cost a new
 * token, and fetched from {@code /connect/token} when neither is usable.
 *
 * <p>Three legacy defects are fixed here rather than reproduced:
 * <ul>
 *   <li>The in-memory expiry was recomputed as "now + expires_in" on every
 *       read, so a cached token never looked expired and was sent until LHDN
 *       returned 401. Here the expiry is computed once, when the token arrives,
 *       and a {@code tokenRefreshMargin} is taken off it.</li>
 *   <li>The database mirror was read and written without a company filter
 *       (and the UPDATE hit every row). Here it is the company's own
 *       {@code CompanySettings} row, written through JPA with bound values.</li>
 *   <li>A failed token request returned an object with a null token that the
 *       caller tested against {@code ""}, so it went on to make the API call
 *       and crashed. Here a failure is a {@link TokenResult} with a message.</li>
 * </ul>
 *
 * <p>Fetches are serialised per company so a burst of pushes after a restart
 * does not fire a burst of token requests — LHDN rate-limits that endpoint.
 */
@Slf4j
@Component
public class MyInvoisTokenProvider {

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);

    private final MyInvoisProperties properties;
    private final MyInvoisUrls urls;
    private final CompanySettingsRepository companySettings;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final RestClient restClient;

    /** One entry per company. The value is immutable; replacing it is atomic. */
    private final ConcurrentMap<Integer, CachedToken> cache = new ConcurrentHashMap<>();

    @Autowired
    public MyInvoisTokenProvider(MyInvoisProperties properties,
                                 MyInvoisUrls urls,
                                 CompanySettingsRepository companySettings,
                                 ObjectMapper objectMapper) {
        this(properties, urls, companySettings, objectMapper, Clock.systemUTC(),
                RestClient.builder().requestFactory(timeoutFactory(properties)));
    }

    /** Visible for tests: inject a fixed clock and a mock-bound RestClient builder. */
    MyInvoisTokenProvider(MyInvoisProperties properties,
                          MyInvoisUrls urls,
                          CompanySettingsRepository companySettings,
                          ObjectMapper objectMapper,
                          Clock clock,
                          RestClient.Builder builder) {
        this.properties = properties;
        this.urls = urls;
        this.companySettings = companySettings;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.restClient = builder.build();
    }

    private static SimpleClientHttpRequestFactory timeoutFactory(MyInvoisProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(properties.getTimeout());
        return factory;
    }

    /** A usable token, or the reason there is none. Never throws. */
    public TokenResult accessToken(Integer companyId) {
        CachedToken cached = cache.get(companyId);
        if (cached != null && cached.isUsable(clock.instant())) {
            return TokenResult.ok(cached.accessToken());
        }
        synchronized (lockFor(companyId)) {
            cached = cache.get(companyId);
            if (cached != null && cached.isUsable(clock.instant())) {
                return TokenResult.ok(cached.accessToken());
            }
            Optional<CachedToken> stored = readStoredToken(companyId);
            if (stored.isPresent()) {
                cache.put(companyId, stored.get());
                return TokenResult.ok(stored.get().accessToken());
            }
            return fetchAndCache(companyId);
        }
    }

    /**
     * Replaces a token LHDN has just rejected. Used after a 401.
     *
     * <p>If another request already replaced that token while this one was in
     * flight, the replacement is returned without a second fetch — N requests
     * failing on the same expired token cost one token call, not N.
     */
    public TokenResult refresh(Integer companyId, String rejectedToken) {
        synchronized (lockFor(companyId)) {
            CachedToken cached = cache.get(companyId);
            if (cached != null && cached.isUsable(clock.instant())
                    && rejectedToken != null && !rejectedToken.equals(cached.accessToken())) {
                return TokenResult.ok(cached.accessToken());
            }
            cache.remove(companyId);
            return fetchAndCache(companyId);
        }
    }

    /** Whether a token is currently cached in memory, for the status endpoint. */
    public boolean hasCachedToken(Integer companyId) {
        CachedToken cached = cache.get(companyId);
        return cached != null && cached.isUsable(clock.instant());
    }

    // ───────────────────────────────────────────────────────────── internals ──

    private TokenResult fetchAndCache(Integer companyId) {
        if (properties.getClientId() == null || properties.getClientId().isBlank()
                || properties.getClientSecret() == null || properties.getClientSecret().isBlank()) {
            return TokenResult.failed("MyInvois client credentials are not configured "
                    + "(myinvois.client-id / myinvois.client-secret)");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("grant_type", "client_credentials");
        form.add("scope", "InvoicingAPI");

        String url = null;
        try {
            url = urls.token();
            return restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .exchange((request, response) -> {
                        int status = response.getStatusCode().value();
                        String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        if (status < 200 || status >= 300) {
                            // The secret is in the request, never in the response; the
                            // body is safe to log and is the only diagnostic LHDN gives.
                            log.error("MyInvois token request failed: HTTP {} {}", status,
                                    MyInvoisErrors.abbreviate(body, 300));
                            return TokenResult.failed("Token generation failed (HTTP " + status + "): "
                                    + MyInvoisErrors.describeHttpError(objectMapper, status, body));
                        }
                        TokenResponse token = objectMapper.readValue(body, TokenResponse.class);
                        if (token == null || token.getAccessToken() == null || token.getAccessToken().isBlank()) {
                            log.error("MyInvois token response carried no access_token");
                            return TokenResult.failed("Token generation failed: LHDN returned no access token");
                        }
                        Instant obtainedAt = clock.instant();
                        CachedToken fresh = CachedToken.from(token, obtainedAt, properties.getTokenRefreshMargin());
                        cache.put(companyId, fresh);
                        // The column holds LHDN's real expiry (as legacy wrote it);
                        // the safety margin is applied when a token is read back.
                        storeToken(companyId, token, CachedToken.realExpiry(token, obtainedAt));
                        log.info("MyInvois token obtained for company {} ({}); expires {}",
                                companyId, urls.environment(), fresh.expiresAt());
                        return TokenResult.ok(fresh.accessToken());
                    });
        } catch (Exception ex) {
            Throwable root = rootCause(ex);
            log.error("MyInvois token request could not be sent to {}", url, ex);
            return TokenResult.failed("Token generation failed: " + root.getMessage());
        }
    }

    /**
     * The company's persisted token, if it is still good. Legacy read the first
     * row of CompanySettings regardless of company; here it is the company's own
     * row, and only when that row has both a token and a parseable expiry.
     */
    private Optional<CachedToken> readStoredToken(Integer companyId) {
        try {
            return companySettings.findByCompanyRefId(companyId)
                    .flatMap(row -> CachedToken.fromStored(row.getAccessToken(), row.getExpiryDateTimeUtc(),
                            properties.getTokenRefreshMargin()))
                    .filter(token -> token.isUsable(clock.instant()));
        } catch (Exception ex) {
            log.warn("Could not read the stored MyInvois token for company {}: {}", companyId, ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Mirrors the token to CompanySettings so the next process start reuses it.
     * A failure here is logged and ignored: the token is already cached in
     * memory and the push must not fail because a settings row is missing.
     */
    private void storeToken(Integer companyId, TokenResponse token, Instant expiresAt) {
        try {
            Optional<CompanySettings> row = companySettings.findByCompanyRefId(companyId);
            if (row.isEmpty()) {
                log.warn("No CompanySettings row for company {}; MyInvois token kept in memory only", companyId);
                return;
            }
            CompanySettings settings = row.get();
            settings.setAccessToken(token.getAccessToken());
            settings.setExpiresIn(token.getExpiresIn() == null ? null : String.valueOf(token.getExpiresIn()));
            settings.setTokenType(token.getTokenType());
            settings.setScope(token.getScope());
            settings.setExpiryDateTimeUtc(expiresAt.toString());
            companySettings.save(settings);
        } catch (Exception ex) {
            log.warn("Could not persist the MyInvois token for company {}: {}", companyId, ex.getMessage());
        }
    }

    private final ConcurrentMap<Integer, Object> locks = new ConcurrentHashMap<>();

    private Object lockFor(Integer companyId) {
        return locks.computeIfAbsent(companyId, id -> new Object());
    }

    private static Throwable rootCause(Throwable ex) {
        Throwable current = ex;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    // ────────────────────────────────────────────────────────────── types ──

    /** Outcome of asking for a token. */
    public record TokenResult(String accessToken, String failure) {

        static TokenResult ok(String accessToken) {
            return new TokenResult(accessToken, null);
        }

        static TokenResult failed(String failure) {
            return new TokenResult(null, failure);
        }

        public boolean success() {
            return accessToken != null && !accessToken.isBlank();
        }
    }

    /**
     * A token with the instant after which it must not be used. The expiry is
     * fixed at creation — this is the fix for the legacy sliding expiry.
     */
    record CachedToken(String accessToken, Instant expiresAt) {

        static CachedToken from(TokenResponse token, Instant obtainedAt, Duration margin) {
            return new CachedToken(token.getAccessToken(), realExpiry(token, obtainedAt).minus(margin));
        }

        /** When LHDN says the token stops working: obtained + expires_in, no margin. */
        static Instant realExpiry(TokenResponse token, Instant obtainedAt) {
            long seconds = token.getExpiresIn() == null ? 0 : token.getExpiresIn();
            return obtainedAt.plusSeconds(seconds);
        }

        static Optional<CachedToken> fromStored(String accessToken, String expiryIso, Duration margin) {
            if (accessToken == null || accessToken.isBlank() || expiryIso == null || expiryIso.isBlank()) {
                return Optional.empty();
            }
            try {
                // Legacy wrote .NET round-trip format ("o"), which Instant parses;
                // this class writes Instant.toString(), which it also parses.
                Instant expiresAt = Instant.parse(expiryIso.trim()).minus(margin);
                return Optional.of(new CachedToken(accessToken, expiresAt));
            } catch (DateTimeParseException ex) {
                return Optional.empty();
            }
        }

        boolean isUsable(Instant now) {
            return accessToken != null && !accessToken.isBlank() && now.isBefore(expiresAt);
        }
    }
}
