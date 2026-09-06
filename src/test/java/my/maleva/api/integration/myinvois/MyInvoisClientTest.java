package my.maleva.api.integration.myinvois;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.common.config.MyInvoisProperties;
import my.maleva.api.module.company.repository.CompanySettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pins the transport contract: bearer header, 401 refresh-and-retry-once,
 * 429 named, LHDN's error envelope rendered, disabled gate, and — above all —
 * that nothing is ever re-POSTed on its own.
 */
class MyInvoisClientTest {

    private static final String URL = "https://preprod-api.myinvois.hasil.gov.my/api/v1.0/documentsubmissions";

    private MyInvoisProperties properties;
    private MockRestServiceServer server;
    private StubTokens tokens;
    private MyInvoisClient client;

    @BeforeEach
    void setUp() {
        properties = new MyInvoisProperties();
        properties.setEnabled(true);
        properties.setEnvironment("preprod");

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        tokens = new StubTokens(properties);
        client = new MyInvoisClient(properties, tokens, new ObjectMapper(), builder);
    }

    @Test
    void sendsBearerTokenAndReturnsBodyOn2xx() {
        server.expect(requestTo(URL))
                .andExpect(method(POST))
                .andExpect(header("Authorization", "Bearer token-1"))
                .andExpect(header("Accept", "application/json"))
                .andRespond(withStatus(HttpStatus.ACCEPTED).body("{\"submissionUid\":\"S1\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        MyInvoisResult result = client.post(URL, java.util.Map.of("documents", List.of()), 1);

        assertThat(result.success()).isTrue();
        assertThat(result.status()).isEqualTo(202);
        assertThat(result.body()).contains("S1");
        server.verify();
    }

    @Test
    void on401RefreshesTheTokenAndRetriesExactlyOnce() {
        server.expect(requestTo(URL)).andExpect(header("Authorization", "Bearer token-1"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(requestTo(URL)).andExpect(header("Authorization", "Bearer token-2"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        MyInvoisResult result = client.post(URL, java.util.Map.of(), 1);

        assertThat(result.success()).isTrue();
        assertThat(tokens.refreshes).isEqualTo(1);
        server.verify();
    }

    @Test
    void aSecond401IsReportedNotRetriedAgain() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        MyInvoisResult result = client.post(URL, java.util.Map.of(), 1);

        assertThat(result.success()).isFalse();
        assertThat(result.status()).isEqualTo(401);
        assertThat(tokens.refreshes).isEqualTo(1);
        server.verify();
    }

    @Test
    void rateLimitIsNamedWithRetryAfterAndNotRetried() {
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "30"));

        MyInvoisResult result = client.post(URL, java.util.Map.of(), 1);

        assertThat(result.success()).isFalse();
        assertThat(result.isRateLimited()).isTrue();
        assertThat(result.message()).contains("rate-limiting").contains("30 seconds");
        server.verify();
    }

    @Test
    void lhdnErrorEnvelopeIsRenderedInTheLegacyFormat() {
        String body = "{\"error\":{\"code\":\"BadArgument\",\"message\":\"Document validation failed\","
                + "\"details\":[{\"code\":\"CF364\",\"target\":\"InvoiceLine[0]\",\"message\":\"Bad classification\"}]}}";
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body(body).contentType(MediaType.APPLICATION_JSON));

        MyInvoisResult result = client.post(URL, java.util.Map.of(), 1);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).isEqualTo(
                "Error Code: BadArgument\n"
                        + "Message: Document validation failed\n"
                        + " - Detail Code: CF364\n"
                        + "   Target: InvoiceLine[0]\n"
                        + "   Message: Bad classification");
    }

    @Test
    void nonEnvelopeErrorBodyIsDescribedNotCrashedOn() {
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY).body("<html>gateway</html>"));

        MyInvoisResult result = client.post(URL, java.util.Map.of(), 1);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("HTTP 502");
    }

    @Test
    void disabledIntegrationNeverCallsOut() {
        properties.setEnabled(false);

        MyInvoisResult result = client.post(URL, java.util.Map.of(), 1);

        assertThat(result.success()).isFalse();
        assertThat(result.status()).isZero();
        assertThat(result.message()).isEqualTo(MyInvoisClient.DISABLED_MESSAGE);
        server.verify(); // no expectations were registered, so any request would fail
    }

    @Test
    void missingTokenIsReportedWithoutCallingOut() {
        tokens.fail = "Token generation failed (HTTP 400): invalid_client";

        MyInvoisResult result = client.get(URL, 1);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("invalid_client");
        server.verify();
    }

    /** A token provider that hands out token-1, then token-2 on refresh, without HTTP. */
    static final class StubTokens extends MyInvoisTokenProvider {
        int refreshes;
        String fail;
        private final List<String> issued = new ArrayList<>(List.of("token-1", "token-2", "token-3"));

        StubTokens(MyInvoisProperties properties) {
            super(properties, new MyInvoisUrls(properties), Mockito.mock(CompanySettingsRepository.class),
                    new ObjectMapper(), Clock.systemUTC(), RestClient.builder());
        }

        @Override
        public TokenResult accessToken(Integer companyId) {
            return fail != null ? new TokenResult(null, fail) : new TokenResult(issued.get(0), null);
        }

        @Override
        public TokenResult refresh(Integer companyId, String rejectedToken) {
            refreshes++;
            issued.remove(0);
            return new TokenResult(issued.get(0), null);
        }
    }
}
