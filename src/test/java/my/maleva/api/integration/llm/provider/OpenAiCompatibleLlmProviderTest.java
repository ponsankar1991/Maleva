package my.maleva.api.integration.llm.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.maleva.api.common.config.LlmProperties;
import my.maleva.api.integration.llm.LlmAttachment;
import my.maleva.api.integration.llm.LlmException;
import my.maleva.api.integration.llm.LlmRequest;
import my.maleva.api.integration.llm.LlmResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiCompatibleLlmProviderTest {

    private static final String URL = "https://llm.test/v1/chat/completions";
    private static final String OK_BODY = """
            {"id":"x","model":"served-model","choices":[{"index":0,"finish_reason":"stop",
             "message":{"role":"assistant","content":"{\\"hello\\":\\"world\\"}"}}],
             "usage":{"prompt_tokens":120,"completion_tokens":8}}
            """;

    private LlmProperties.Provider config;
    private MockRestServiceServer server;
    private OpenAiCompatibleLlmProvider provider;

    @BeforeEach
    void setUp() {
        config = new LlmProperties.Provider();
        config.setType("openai-compatible");
        config.setLabel("Test LLM");
        config.setBaseUrl("https://llm.test/v1/");
        config.setApiKey("test-key");
        config.setModel("text-model");
        config.setVisionModel("vision-model");
        config.setJsonMode("json_object");
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        provider = new OpenAiCompatibleLlmProvider("test", config, new ObjectMapper(), builder);
    }

    @Test
    void textOnlyCallSendsPlainStringContentAndBearerKey() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andExpect(content().string(containsString("\"model\":\"text-model\"")))
                .andExpect(content().string(containsString("\"content\":\"Say hi")))
                .andExpect(content().string(not(containsString("image_url"))))
                .andRespond(withSuccess(OK_BODY, MediaType.APPLICATION_JSON));

        LlmResponse response = provider.complete(LlmRequest.builder().userPrompt("Say hi").build());

        assertThat(response.text()).isEqualTo("{\"hello\":\"world\"}");
        assertThat(response.model()).isEqualTo("served-model");
        assertThat(response.inputTokens()).isEqualTo(120L);
        assertThat(response.outputTokens()).isEqualTo(8L);
        assertThat(response.stopReason()).isEqualTo("stop");
        server.verify();
    }

    @Test
    void imageCallUsesTheVisionModelDataUriAndJsonMode() {
        server.expect(requestTo(URL))
                .andExpect(content().string(containsString("\"model\":\"vision-model\"")))
                .andExpect(content().string(containsString("data:image/png;base64,AQID")))
                .andExpect(content().string(containsString("\"response_format\":{\"type\":\"json_object\"}")))
                .andExpect(content().string(containsString("\"role\":\"system\"")))
                .andRespond(withSuccess(OK_BODY, MediaType.APPLICATION_JSON));

        LlmRequest request = LlmRequest.builder()
                .systemPrompt("You read bills")
                .userPrompt("Read this")
                .jsonOutput(true)
                .attachments(List.of(new LlmAttachment("bill.png", "image/png", new byte[]{1, 2, 3})))
                .build();

        assertThat(provider.complete(request).text()).contains("hello");
        server.verify();
    }

    @Test
    void extractedTextIsInlinedIntoThePrompt() {
        server.expect(requestTo(URL))
                .andExpect(content().string(containsString("Document text (bill.pdf)")))
                .andExpect(content().string(containsString("TOTAL RM 100")))
                .andRespond(withSuccess(OK_BODY, MediaType.APPLICATION_JSON));

        LlmRequest request = LlmRequest.builder()
                .userPrompt("Read this")
                .attachments(List.of(LlmAttachment.text("bill.pdf", "TOTAL RM 100")))
                .build();

        provider.complete(request);
        server.verify();
    }

    @Test
    void rateLimitBecomesRateLimited() {
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .body("{\"error\":{\"message\":\"slow down\"}}").contentType(MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.complete(LlmRequest.builder().userPrompt("x").build()))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("slow down")
                .extracting(ex -> ((LlmException) ex).getKind())
                .isEqualTo(LlmException.Kind.RATE_LIMITED);
    }

    @Test
    void badKeyBecomesAuthentication() {
        server.expect(requestTo(URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("{\"error\":\"invalid api key\"}"));

        assertThatThrownBy(() -> provider.complete(LlmRequest.builder().userPrompt("x").build()))
                .extracting(ex -> ((LlmException) ex).getKind())
                .isEqualTo(LlmException.Kind.AUTHENTICATION);
    }

    @Test
    void missingKeyIsNotConfiguredAndNeverCallsOut() {
        config.setApiKey("");
        assertThat(provider.info().configured()).isFalse();
        assertThatThrownBy(() -> provider.complete(LlmRequest.builder().userPrompt("x").build()))
                .extracting(ex -> ((LlmException) ex).getKind())
                .isEqualTo(LlmException.Kind.NOT_CONFIGURED);
        server.verify();
    }

    @Test
    void optionalKeyProviderIsConfiguredWithoutOne() {
        config.setApiKey("");
        config.setRequireApiKey(false);
        assertThat(provider.info().configured()).isTrue();
        server.expect(requestTo(URL))
                .andExpect(header("Authorization", "Bearer ollama"))
                .andRespond(withSuccess(OK_BODY, MediaType.APPLICATION_JSON));
        provider.complete(LlmRequest.builder().userPrompt("x").build());
        server.verify();
    }

    @Test
    void textOnlyProviderRejectsImages() {
        config.setVisionModel(null);
        config.setSupportsVision(false);
        LlmRequest request = LlmRequest.builder()
                .userPrompt("Read this")
                .attachments(List.of(new LlmAttachment("bill.png", "image/png", "x".getBytes(StandardCharsets.UTF_8))))
                .build();
        assertThatThrownBy(() -> provider.complete(request))
                .extracting(ex -> ((LlmException) ex).getKind())
                .isEqualTo(LlmException.Kind.UNSUPPORTED_INPUT);
    }

    @Test
    void arrayContentPartsAreJoined() {
        String body = """
                {"choices":[{"message":{"content":[{"type":"text","text":"{\\"a\\":"},{"type":"text","text":"1}"}]}}]}
                """;
        server.expect(requestTo(URL)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        assertThat(provider.complete(LlmRequest.builder().userPrompt("x").build()).text()).isEqualTo("{\"a\":1}");
    }
}
