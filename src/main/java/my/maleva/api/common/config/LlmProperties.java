package my.maleva.api.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for the pluggable LLM layer ({@code integration/llm}).
 *
 * <p>Every provider is declared under {@code llm.providers.<key>} and is one of
 * three types: {@code claude} (official Anthropic SDK), {@code openai-compatible}
 * (any server that speaks the OpenAI chat-completions wire format: DeepSeek,
 * Ollama, Groq, Gemini, OpenRouter, ...) or {@code stub} (returns sample data,
 * never calls out - for testing with no API key at all).
 *
 * <p>Which provider handles a call is decided at runtime, in this order:
 * explicit request override, the company's stored preference for the task,
 * the company's stored default, {@code llm.tasks.<task>}, then
 * {@code llm.default-provider}. See {@code LlmGateway}.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /** Master switch. When false every call fails fast with a clear message. */
    private boolean enabled = true;

    /** Provider key used when nothing more specific is configured. */
    private String defaultProvider = "claude";

    /**
     * Providers tried, in order, when the chosen one fails (not configured,
     * rate limited, provider error, refusal). Empty = no fallback.
     */
    private List<String> fallbackProviders = new ArrayList<>();

    /** Per-task provider overrides from configuration: task key to provider key. */
    private Map<String, String> tasks = new LinkedHashMap<>();

    /** Declared providers keyed by their short name ("claude", "ollama", ...). */
    private Map<String, Provider> providers = new LinkedHashMap<>();

    /**
     * When true the prompt text is written to the log at DEBUG. Off by default
     * because prompts carry supplier and bank details.
     */
    private boolean logPrompts = false;

    /** How many recent calls the in-memory call log keeps for the settings screen. */
    private int callLogSize = 200;

    @Getter
    @Setter
    public static class Provider {
        /** claude | openai-compatible | stub */
        private String type = "openai-compatible";
        /** Display name for the settings screen; defaults to the key. */
        private String label;
        private boolean enabled = true;
        private String apiKey = "";
        /** Ollama accepts any bearer value, so its key is optional. */
        private boolean requireApiKey = true;
        /** OpenAI-compatible only: root such as https://api.deepseek.com (no trailing /chat/completions). */
        private String baseUrl;
        /** Model used for text-only calls. */
        private String model;
        /** Model used when the request carries images; blank = use {@link #model}. */
        private String visionModel;
        private boolean supportsVision = false;
        /** Whether the provider accepts a PDF directly. Otherwise PDFs are rasterised or text-extracted first. */
        private boolean supportsPdf = false;
        /**
         * OpenAI-compatible only: none | json_object | json_schema. Controls the
         * {@code response_format} sent when the caller asks for JSON output.
         */
        private String jsonMode = "json_object";
        private Integer maxTokens = 8192;
        private int timeoutSeconds = 180;
        /** Claude only: low | medium | high | xhigh | max. Blank = API default. */
        private String effort;
        /** Flag for the settings screen: runs locally or on a free tier. */
        private boolean free = false;
        /** Free-text shown on the settings screen (data policy, limits). */
        private String note;
        /** Extra HTTP headers, e.g. OpenRouter's HTTP-Referer. */
        private Map<String, String> headers = new LinkedHashMap<>();
    }
}
