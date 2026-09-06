package my.maleva.api.integration.myinvois;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.common.config.MyInvoisProperties;
import my.maleva.api.module.company.entity.CompanySettings;
import my.maleva.api.module.company.repository.CompanySettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The token lifecycle, with the three legacy defects pinned as fixed: fixed
 * expiry (not sliding), company-scoped persistence, and a failure that is a
 * result rather than a null dereference downstream.
 */
class MyInvoisTokenProviderTest {

    private static final String TOKEN_URL = "https://preprod-api.myinvois.hasil.gov.my/connect/token";
    private static final Instant T0 = Instant.parse("2026-09-05T02:00:00Z");

    private MyInvoisProperties properties;
    private CompanySettingsRepository settings;
    private MockRestServiceServer server;
    private MutableClock clock;
    private MyInvoisTokenProvider provider;

    @BeforeEach
    void setUp() {
        properties = new MyInvoisProperties();
        properties.setEnvironment("preprod");
        properties.setClientId("client-id");
        properties.setClientSecret("client-secret");
        properties.setTokenRefreshMargin(Duration.ofSeconds(60));

        settings = Mockito.mock(CompanySettingsRepository.class);
        when(settings.findByCompanyRefId(any())).thenReturn(Optional.empty());

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        clock = new MutableClock(T0);
        provider = new MyInvoisTokenProvider(properties, new MyInvoisUrls(properties), settings,
                new ObjectMapper(), clock, builder);
    }

    @Test
    void fetchesWithClientCredentialsFormAndCachesUntilExpiryMinusMargin() {
        expectTokenRequest("tok-1", 3600);

        assertThat(provider.accessToken(1).accessToken()).isEqualTo("tok-1");
        assertThat(provider.accessToken(1).accessToken()).isEqualTo("tok-1"); // cached, no second request
        server.verify();

        // Legacy's sliding expiry would still call this valid; here it expires at 3600 - 60 s.
        clock.now = T0.plusSeconds(3539);
        assertThat(provider.hasCachedToken(1)).isTrue();
        clock.now = T0.plusSeconds(3541);
        assertThat(provider.hasCachedToken(1)).isFalse();
    }

    @Test
    void refreshDiscardsTheCacheAndFetchesAgain() {
        expectTokenRequest("tok-1", 3600);
        expectTokenRequest("tok-2", 3600);

        assertThat(provider.accessToken(1).accessToken()).isEqualTo("tok-1");
        assertThat(provider.refresh(1, "tok-1").accessToken()).isEqualTo("tok-2");
        assertThat(provider.accessToken(1).accessToken()).isEqualTo("tok-2");
        server.verify();
    }

    @Test
    void aRefreshForAnAlreadyReplacedTokenDoesNotFetchAgain() {
        expectTokenRequest("tok-1", 3600);
        expectTokenRequest("tok-2", 3600);

        provider.accessToken(1);
        provider.refresh(1, "tok-1");                                   // first 401: fetches tok-2
        assertThat(provider.refresh(1, "tok-1").accessToken()).isEqualTo("tok-2"); // second 401 on the OLD token: reuse
        server.verify();
    }

    @Test
    void aStoredUnexpiredTokenIsReusedWithoutCallingLhdn() {
        CompanySettings row = new CompanySettings();
        row.setCompanyRefId(1);
        row.setAccessToken("stored-tok");
        row.setExpiryDateTimeUtc(T0.plusSeconds(1800).toString());
        when(settings.findByCompanyRefId(1)).thenReturn(Optional.of(row));

        assertThat(provider.accessToken(1).accessToken()).isEqualTo("stored-tok");
        server.verify(); // nothing was requested
    }

    @Test
    void aStoredExpiredTokenIsIgnored() {
        CompanySettings row = new CompanySettings();
        row.setCompanyRefId(1);
        row.setAccessToken("old-tok");
        row.setExpiryDateTimeUtc(T0.minusSeconds(1).toString());
        when(settings.findByCompanyRefId(1)).thenReturn(Optional.of(row));
        expectTokenRequest("tok-1", 3600);

        assertThat(provider.accessToken(1).accessToken()).isEqualTo("tok-1");
        // and the fresh token is written back to the same company's row
        verify(settings).save(row);
        assertThat(row.getAccessToken()).isEqualTo("tok-1");
        // the column holds LHDN's real expiry; the 60 s margin is applied on read, not on write
        assertThat(row.getExpiryDateTimeUtc()).isEqualTo(T0.plusSeconds(3600).toString());
    }

    @Test
    void failedTokenRequestIsAResultNotAnException() {
        server.expect(requestTo(TOKEN_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("{\"error\":\"invalid_client\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        MyInvoisTokenProvider.TokenResult result = provider.accessToken(1);

        assertThat(result.success()).isFalse();
        assertThat(result.failure()).contains("Token generation failed").contains("HTTP 400");
        verify(settings, never()).save(any());
    }

    @Test
    void missingCredentialsAreReportedWithoutCallingOut() {
        properties.setClientSecret("");

        MyInvoisTokenProvider.TokenResult result = provider.accessToken(1);

        assertThat(result.success()).isFalse();
        assertThat(result.failure()).contains("myinvois.client-secret");
        server.verify();
    }

    private void expectTokenRequest(String token, int expiresIn) {
        server.expect(requestTo(TOKEN_URL))
                .andExpect(method(POST))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("grant_type=client_credentials")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("scope=InvoicingAPI")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("client_id=client-id")))
                .andRespond(withSuccess("{\"access_token\":\"" + token + "\",\"expires_in\":" + expiresIn
                        + ",\"token_type\":\"Bearer\",\"scope\":\"InvoicingAPI\"}", MediaType.APPLICATION_JSON));
    }

    /** A clock the test can move. */
    static final class MutableClock extends Clock {
        Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
