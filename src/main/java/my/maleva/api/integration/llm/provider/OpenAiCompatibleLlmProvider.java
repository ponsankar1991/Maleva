package my.maleva.api.integration.llm.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.LlmProperties;
import my.maleva.api.integration.llm.LlmAttachment;
import my.maleva.api.integration.llm.LlmException;
import my.maleva.api.integration.llm.LlmKeyMask;
import my.maleva.api.integration.llm.LlmProvider;
import my.maleva.api.integration.llm.LlmProviderInfo;
import my.maleva.api.integration.llm.LlmRequest;
import my.maleva.api.integration.llm.LlmResponse;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Any server that speaks the OpenAI chat-completions wire format: DeepSeek,
 * Ollama (local), Groq, Google Gemini's OpenAI endpoint, OpenRouter, and
 * OpenAI itself. Images go as {@code data:} URIs; PDFs go as a {@code file}
 * content part only when the provider says it supports them (OpenRouter),
 * otherwise the gateway's document adapter has already rasterised them.
 */
@Slf4j
public class OpenAiCompatibleLlmProvider implements LlmProvider {

    public static final String TYPE = "openai-compatible";
    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);

    private final String key;
    private final LlmProperties.Provider config;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAiCompatibleLlmProvider(String key, LlmProperties.Provider config, ObjectMapper objectMapper) {
        this(key, config, objectMapper, RestClient.builder().requestFactory(timeoutFactory(config)));
    }

    /** Visible for tests, which bind a MockRestServiceServer onto the builder. */
    OpenAiCompatibleLlmProvider(String key, LlmProperties.Provider config, ObjectMapper objectMapper,
                                RestClient.Builder builder) {
        this.key = key;
        this.config = config;
        this.objectMapper = objectMapper;
        this.restClient = builder.build();
    }

    private static SimpleClientHttpRequestFactory timeoutFactory(LlmProperties.Provider config) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(Duration.ofSeconds(Math.max(10, config.getTimeoutSeconds())));
        return factory;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public LlmProviderInfo info() {
        boolean hasKey = !config.isRequireApiKey() || notBlank(config.getApiKey());
        boolean configured = config.isEnabled() && notBlank(config.getBaseUrl()) && hasKey && notBlank(config.getModel());
        boolean vision = config.isSupportsVision() || notBlank(config.getVisionModel());
        String label = notBlank(config.getLabel()) ? config.getLabel() : key;
        return new LlmProviderInfo(key, label, TYPE, config.getModel(), blankToNull(config.getVisionModel()),
                vision, config.isSupportsPdf(), configured, config.isFree(), config.getNote());
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        LlmProviderInfo info = info();
        if (!info.configured()) {
            throw new LlmException(LlmException.Kind.NOT_CONFIGURED, key,
                    info.label() + " is not configured: set llm.providers." + key
                            + ".base-url, .model" + (config.isRequireApiKey() ? " and .api-key" : "")
                            + " (see application.yaml) or choose another provider in AI Settings");
        }

        List<Map<String, Object>> parts = new ArrayList<>();
        StringBuilder inlineText = new StringBuilder();
        boolean binary = false;
        for (LlmAttachment attachment : request.getAttachments()) {
            if (attachment.isImage()) {
                if (!info.supportsVision()) {
                    throw new LlmException(LlmException.Kind.UNSUPPORTED_INPUT, key,
                            info.label() + " cannot read images; choose a provider with a vision model");
                }
                binary = true;
                parts.add(Map.of("type", "image_url", "image_url",
                        Map.of("url", "data:" + attachment.normalizedMediaType() + ";base64," + attachment.base64())));
            } else if (attachment.isPdf()) {
                if (!info.supportsPdf()) {
                    throw new LlmException(LlmException.Kind.UNSUPPORTED_INPUT, key,
                            info.label() + " cannot read PDF files directly");
                }
                binary = true;
                parts.add(Map.of("type", "file", "file", Map.of(
                        "filename", attachment.fileName() == null ? "document.pdf" : attachment.fileName(),
                        "file_data", "data:application/pdf;base64," + attachment.base64())));
            } else if (attachment.isText()) {
                inlineText.append("\n\n--- Document text (").append(attachment.fileName()).append(") ---\n")
                        .append(attachment.asText());
            } else {
                throw new LlmException(LlmException.Kind.UNSUPPORTED_INPUT, key,
                        info.label() + " cannot read attachments of type " + attachment.mediaType());
            }
        }

        String prompt = request.getUserPrompt() == null ? "" : request.getUserPrompt();
        if (request.isJsonOutput()) {
            prompt = prompt + "\n\nRespond with a single JSON object and nothing else - no markdown fences, no commentary.";
        }
        String userText = prompt + inlineText;

        List<Map<String, Object>> messages = new ArrayList<>();
        if (notBlank(request.getSystemPrompt())) {
            messages.add(Map.of("role", "system", "content", request.getSystemPrompt()));
        }
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("role", "user");
        if (parts.isEmpty()) {
            // Plain string content: the most widely accepted shape for text-only calls.
            user.put("content", userText);
        } else {
            List<Map<String, Object>> content = new ArrayList<>();
            content.add(Map.of("type", "text", "text", userText));
            content.addAll(parts);
            user.put("content", content);
        }
        messages.add(user);

        String model = binary && notBlank(config.getVisionModel()) ? config.getVisionModel().trim() : config.getModel().trim();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens()
                : (config.getMaxTokens() != null ? config.getMaxTokens() : 4096));
        body.put("stream", false);
        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        }
        if (request.isJsonOutput()) {
            responseFormat(request).ifPresent(format -> body.put("response_format", format));
        }

        String url = config.getBaseUrl().trim().replaceAll("/+$", "") + "/chat/completions";
        String bearer = notBlank(config.getApiKey()) ? config.getApiKey().trim() : "ollama";

        long start = System.nanoTime();
        try {
            RestClient.RequestBodySpec spec = restClient.post().uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + bearer);
            for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
                spec = spec.header(header.getKey(), header.getValue());
            }
            String json = objectMapper.writeValueAsString(body);
            return spec.body(json).exchange((clientRequest, clientResponse) -> {
                int status = clientResponse.getStatusCode().value();
                String responseBody = new String(clientResponse.getBody().readAllBytes(), StandardCharsets.UTF_8);
                long latency = (System.nanoTime() - start) / 1_000_000;
                if (status < 200 || status >= 300) {
                    throw toException(status, responseBody);
                }
                return parseResponse(responseBody, model, latency);
            });
        } catch (LlmException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            boolean timeout = rootCause(ex) instanceof SocketTimeoutException;
            throw new LlmException(timeout ? LlmException.Kind.TIMEOUT : LlmException.Kind.PROVIDER_ERROR, key,
                    (timeout ? "Timed out waiting for " : "Could not connect to ") + info.label() + " at "
                            + config.getBaseUrl() + ": " + rootCause(ex).getMessage(), ex);
        } catch (Exception ex) {
            throw new LlmException(LlmException.Kind.PROVIDER_ERROR, key,
                    info.label() + " call failed: " + rootCause(ex).getMessage(), ex);
        }
    }

    private java.util.Optional<Map<String, Object>> responseFormat(LlmRequest request) {
        String mode = config.getJsonMode() == null ? "json_object" : config.getJsonMode().trim().toLowerCase(Locale.ROOT);
        switch (mode) {
            case "none":
            case "":
                return java.util.Optional.empty();
            case "json_schema":
                if (request.getJsonSchema() != null) {
                    Map<String, Object> schema = new LinkedHashMap<>();
                    schema.put("name", "result");
                    schema.put("schema", request.getJsonSchema());
                    return java.util.Optional.of(Map.of("type", "json_schema", "json_schema", schema));
                }
                return java.util.Optional.of(Map.of("type", "json_object"));
            default:
                return java.util.Optional.of(Map.of("type", "json_object"));
        }
    }

    private LlmException toException(int status, String body) {
        String detail = errorMessage(body);
        String keyHint = " [key used: " + LlmKeyMask.fingerprint(config.getApiKey()) + "]";
        if (status == 400 && detail.toLowerCase(Locale.ROOT).contains("api key")) {
            // Google answers a malformed or revoked key with 400 INVALID_ARGUMENT, not 401.
            return new LlmException(LlmException.Kind.AUTHENTICATION, key,
                    info().label() + " did not accept the API key (HTTP 400): " + detail + keyHint);
        }
        switch (status) {
            case 401:
            case 403:
                return new LlmException(LlmException.Kind.AUTHENTICATION, key,
                        info().label() + " rejected the API key (HTTP " + status + "): " + detail + keyHint);
            case 429:
                return new LlmException(LlmException.Kind.RATE_LIMITED, key,
                        info().label() + " rate limit reached (HTTP 429): " + detail);
            case 404:
                return new LlmException(LlmException.Kind.PROVIDER_ERROR, key,
                        info().label() + " returned 404 - check base-url and model name: " + detail);
            default:
                return new LlmException(LlmException.Kind.PROVIDER_ERROR, key,
                        info().label() + " returned HTTP " + status + ": " + detail);
        }
    }

    private String errorMessage(String body) {
        if (body == null || body.isBlank()) {
            return "(empty body)";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode error = root.path("error");
            if (error.isTextual()) {
                return error.asText();
            }
            if (error.path("message").isTextual()) {
                return error.path("message").asText();
            }
            if (root.path("message").isTextual()) {
                return root.path("message").asText();
            }
        } catch (Exception ignored) {
            // not JSON - fall through to the raw body
        }
        return body.length() > 300 ? body.substring(0, 300) + "..." : body;
    }

    private LlmResponse parseResponse(String body, String requestedModel, long latency) {
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode choice = root.path("choices").path(0);
            JsonNode message = choice.path("message");
            String text = contentText(message.path("content"));
            if (text.isBlank() && message.path("reasoning_content").isTextual()) {
                // Reasoning models sometimes spend the whole budget thinking.
                throw new LlmException(LlmException.Kind.BAD_RESPONSE, key,
                        info().label() + " returned reasoning but no answer - raise max-tokens or use a non-reasoning model");
            }
            String finish = choice.path("finish_reason").asText("");
            JsonNode usage = root.path("usage");
            Long inputTokens = usage.path("prompt_tokens").isNumber() ? usage.path("prompt_tokens").asLong() : null;
            Long outputTokens = usage.path("completion_tokens").isNumber() ? usage.path("completion_tokens").asLong() : null;
            String model = root.path("model").isTextual() ? root.path("model").asText() : requestedModel;
            return new LlmResponse(key, model, text, inputTokens, outputTokens, latency, finish);
        } catch (LlmException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LlmException(LlmException.Kind.BAD_RESPONSE, key,
                    info().label() + " returned an unreadable response: " + ex.getMessage(), ex);
        }
    }

    private static String contentText(JsonNode content) {
        if (content.isTextual()) {
            return content.asText();
        }
        if (content.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode part : content) {
                if (part.path("text").isTextual()) {
                    sb.append(part.path("text").asText());
                }
            }
            return sb.toString();
        }
        return "";
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String blankToNull(String value) {
        return notBlank(value) ? value.trim() : null;
    }

    private static Throwable rootCause(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t;
    }
}
