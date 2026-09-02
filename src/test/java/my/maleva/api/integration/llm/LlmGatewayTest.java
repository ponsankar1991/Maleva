package my.maleva.api.integration.llm;

import my.maleva.api.common.config.LlmProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LlmGatewayTest {

    /** A provider that either answers or throws a given exception, and records what it was asked. */
    static class FakeProvider implements LlmProvider {
        final String key;
        final boolean configured;
        final boolean vision;
        LlmException failure;
        final List<LlmRequest> calls = new ArrayList<>();

        FakeProvider(String key, boolean configured, boolean vision) {
            this.key = key;
            this.configured = configured;
            this.vision = vision;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public LlmProviderInfo info() {
            return new LlmProviderInfo(key, key, "fake", "model-" + key, null, vision, false, configured, false, null);
        }

        @Override
        public LlmResponse complete(LlmRequest request) {
            calls.add(request);
            if (!configured) {
                throw new LlmException(LlmException.Kind.NOT_CONFIGURED, key, key + " not configured");
            }
            if (failure != null) {
                throw failure;
            }
            return new LlmResponse(key, "model-" + key, "answer from " + key, 10L, 5L, 3, "end_turn");
        }
    }

    @Mock
    private LlmPreferenceStore preferences;

    private LlmProperties properties;
    private FakeProvider claude;
    private FakeProvider ollama;
    private LlmCallLog callLog;
    private LlmGateway gateway;

    @BeforeEach
    void setUp() {
        properties = new LlmProperties();
        properties.setDefaultProvider("claude");
        properties.setFallbackProviders(new ArrayList<>(List.of("ollama")));
        claude = new FakeProvider("claude", false, true);
        ollama = new FakeProvider("ollama", true, true);
        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("claude", claude);
        providers.put("ollama", ollama);
        callLog = new LlmCallLog(10);
        when(preferences.get(any(), any())).thenReturn(Optional.empty());
        gateway = new LlmGateway(properties, new LlmProviderRegistry(providers), preferences, new LlmDocumentAdapter(), callLog);
    }

    private static LlmRequest request(String provider) {
        return LlmRequest.builder().task("bill-extraction").companyRefId(6).providerKey(provider).userPrompt("hi").build();
    }

    @Test
    void fallsBackWhenThePrimaryIsNotConfigured() {
        LlmResponse response = gateway.complete(request(null));

        assertThat(response.providerKey()).isEqualTo("ollama");
        assertThat(claude.calls).hasSize(1);
        assertThat(ollama.calls).hasSize(1);
        assertThat(callLog.recent(10)).hasSize(2);
        assertThat(callLog.recent(10).get(0).success()).isTrue();
        assertThat(callLog.recent(10).get(1).success()).isFalse();
    }

    @Test
    void explicitProviderIsNeverSwapped() {
        assertThatThrownBy(() -> gateway.complete(request("claude")))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("not configured");
        assertThat(ollama.calls).isEmpty();
    }

    @Test
    void reportsThePrimaryErrorWhenEveryProviderFails() {
        ollama.failure = new LlmException(LlmException.Kind.RATE_LIMITED, "ollama", "slow down");

        assertThatThrownBy(() -> gateway.complete(request(null)))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("claude not configured");
    }

    @Test
    void companyPreferenceBeatsTheConfiguredDefault() {
        claude.failure = null;
        when(preferences.get(eq(6), eq("bill-extraction"))).thenReturn(Optional.of("ollama"));

        assertThat(gateway.resolveProviderKey(6, "bill-extraction", null)).isEqualTo("ollama");
        LlmResponse response = gateway.complete(request(null));
        assertThat(response.providerKey()).isEqualTo("ollama");
        assertThat(claude.calls).isEmpty();
    }

    @Test
    void companyDefaultAppliesWhenTheTaskHasNoPreference() {
        when(preferences.get(eq(6), eq(null))).thenReturn(Optional.of("ollama"));
        assertThat(gateway.resolveProviderKey(6, "bill-extraction", null)).isEqualTo("ollama");
    }

    @Test
    void storedPreferenceForUnknownProviderIsIgnored() {
        when(preferences.get(eq(6), any())).thenReturn(Optional.of("gone"));
        assertThat(gateway.resolveProviderKey(6, "bill-extraction", null)).isEqualTo("claude");
    }

    @Test
    void configuredTaskOverrideBeatsTheDefault() {
        properties.getTasks().put("bill-extraction", "ollama");
        assertThat(gateway.resolveProviderKey(null, "bill-extraction", null)).isEqualTo("ollama");
        assertThat(gateway.resolveProviderKey(null, "other", null)).isEqualTo("claude");
    }

    @Test
    void unknownExplicitProviderIsAClearError() {
        assertThatThrownBy(() -> gateway.complete(request("nope")))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("Unknown AI provider 'nope'");
    }

    @Test
    void disabledSwitchStopsEverything() {
        properties.setEnabled(false);
        assertThatThrownBy(() -> gateway.complete(request(null)))
                .isInstanceOf(LlmException.class)
                .extracting(ex -> ((LlmException) ex).getKind())
                .isEqualTo(LlmException.Kind.DISABLED);
        assertThat(claude.calls).isEmpty();
    }

    @Test
    void textOnlyProviderRejectsImagesBeforeCalling() {
        FakeProvider textOnly = new FakeProvider("deepseek", true, false);
        Map<String, LlmProvider> providers = new LinkedHashMap<>();
        providers.put("deepseek", textOnly);
        properties.setFallbackProviders(new ArrayList<>());
        LlmGateway textGateway = new LlmGateway(properties, new LlmProviderRegistry(providers), preferences,
                new LlmDocumentAdapter(), callLog);
        LlmRequest withImage = request("deepseek").toBuilder()
                .attachments(List.of(new LlmAttachment("a.png", "image/png", new byte[]{1, 2, 3})))
                .build();

        assertThatThrownBy(() -> textGateway.complete(withImage))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("cannot read images");
        assertThat(textOnly.calls).isEmpty();
    }
}
