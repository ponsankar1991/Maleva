package my.maleva.api.integration.qne;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import my.maleva.api.common.config.QneProperties;

/**
 * Pins the legacy {@code commonfunctions.QneApi} contract onto
 * {@link QneClient}: the DbCode header and its demo/live switch, body
 * passthrough on 400/404, the {@code code\nMessage} rendering of QNE's error
 * envelope, and the disabled gate. These are behaviours the migrated call
 * sites branch on, so a change here is a change to every QNE screen.
 */
class QneClientTest {

    private static final String URL = "https://qne.test/api/SalesInvoices";

    private QneProperties properties;
    private MockRestServiceServer server;
    private QneClient client;

    @BeforeEach
    void setUp() {
        properties = new QneProperties();
        properties.setEnabled(true);
        properties.setDemo(false);
        QneProperties.Db db = new QneProperties.Db();
        db.setTrial("OUCMLM_TRIAL_V1");
        db.setLive("OUCMLM");
        properties.setDb(db);

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new QneClient(properties, new ObjectMapper(), builder);
    }

    @Test
    void sendsLiveDbCodeHeaderAndReturnsBodyOnSuccess() {
        server.expect(requestTo(URL))
                .andExpect(method(GET))
                .andExpect(header("DbCode", "OUCMLM"))
                .andRespond(withSuccess("[{\"id\":\"abc\"}]", MediaType.APPLICATION_JSON));

        QneResult result = client.get(URL);

        assertThat(result.success()).isTrue();
        assertThat(result.status()).isEqualTo(200);
        // Callers parse ids out of message, so it must be the raw body.
        assertThat(result.message()).isEqualTo("[{\"id\":\"abc\"}]");
        server.verify();
    }

    @Test
    void demoFlagSwitchesToTrialDbCode() {
        properties.setDemo(true);

        server.expect(requestTo(URL))
                .andExpect(header("DbCode", "OUCMLM_TRIAL_V1"))
                .andRespond(withSuccess("ok", MediaType.APPLICATION_JSON));

        assertThat(client.get(URL).success()).isTrue();
        server.verify();
    }

    @Test
    void postSerializesBodyAsJson() {
        server.expect(requestTo(URL))
                .andExpect(method(POST))
                .andExpect(content().json("{\"invoiceCode\":\"INV-1\"}"))
                .andRespond(withSuccess("created", MediaType.APPLICATION_JSON));

        record Payload(String invoiceCode) {}
        QneResult result = client.post(URL, new Payload("INV-1"));

        assertThat(result.success()).isTrue();
        server.verify();
    }

    /**
     * A property-less body serialises as {} instead of failing — Json.NET
     * accepted {@code new object()} and some legacy call sites leaned on it.
     */
    @Test
    void putSerializesEvenAPropertylessBody() {
        server.expect(requestTo(URL))
                .andExpect(method(PUT))
                .andExpect(content().json("{}"))
                .andRespond(withSuccess("updated", MediaType.APPLICATION_JSON));

        assertThat(client.put(URL, new Object()).success()).isTrue();
        server.verify();
    }

    /**
     * 400 and 404 carry QNE's own validation text — a duplicate customer
     * code, a missing account — and the legacy UI showed that text verbatim.
     */
    @Test
    void badRequestAndNotFoundPassTheBodyThrough() {
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .body("Customer code 700-0001 already exists"));

        QneResult result = client.get(URL);

        assertThat(result.success()).isFalse();
        assertThat(result.status()).isEqualTo(400);
        assertThat(result.message()).isEqualTo("Customer code 700-0001 already exists");
    }

    @Test
    void otherHttpErrorsRenderTheQneErrorEnvelope() {
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"code\":\"E500\",\"Message\":\"Posting period closed\"}"));

        QneResult result = client.get(URL);

        assertThat(result.success()).isFalse();
        // Legacy shape: code, newline, Message.
        assertThat(result.message()).isEqualTo("E500\nPosting period closed");
    }

    /**
     * The legacy version deserialised blindly and a non-JSON body (an HTML
     * gateway page) replaced the real error with a parse exception. The port
     * falls back to the raw body instead.
     */
    @Test
    void nonEnvelopeErrorBodyFallsBackToItself() {
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY).body("<html>502</html>"));

        QneResult result = client.get(URL);

        assertThat(result.success()).isFalse();
        assertThat(result.message()).isEqualTo("<html>502</html>");
    }

    /** The legacy per-call-site `if (qneapilist.qneapi)` gate, centralised. */
    @Test
    void disabledIntegrationShortCircuitsWithoutCallingOut() {
        properties.setEnabled(false);
        // No expectation registered: any request would fail the test.

        QneResult result = client.post(URL, new Object());

        assertThat(result.success()).isFalse();
        assertThat(result.status()).isZero();
        assertThat(result.message()).isEqualTo(QneClient.DISABLED_MESSAGE);
        server.verify();
    }

    @Test
    void transportFailureReportsRootCauseInsteadOfThrowing() {
        server.expect(requestTo(URL))
                .andRespond(request -> {
                    throw new java.io.IOException("Connection refused");
                });

        QneResult result = client.get(URL);

        assertThat(result.success()).isFalse();
        assertThat(result.status()).isZero();
        assertThat(result.message()).contains("Connection refused");
    }
}
