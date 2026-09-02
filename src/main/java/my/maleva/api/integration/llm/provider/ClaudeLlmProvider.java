package my.maleva.api.integration.llm.provider;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.errors.AnthropicException;
import com.anthropic.errors.AnthropicIoException;
import com.anthropic.errors.AnthropicServiceException;
import com.anthropic.errors.BadRequestException;
import com.anthropic.errors.InternalServerException;
import com.anthropic.errors.PermissionDeniedException;
import com.anthropic.errors.RateLimitException;
import com.anthropic.errors.UnauthorizedException;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.Base64PdfSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import lombok.extern.slf4j.Slf4j;
import my.maleva.api.common.config.LlmProperties;
import my.maleva.api.integration.llm.LlmAttachment;
import my.maleva.api.integration.llm.LlmException;
import my.maleva.api.integration.llm.LlmKeyMask;
import my.maleva.api.integration.llm.LlmProvider;
import my.maleva.api.integration.llm.LlmProviderInfo;
import my.maleva.api.integration.llm.LlmRequest;
import my.maleva.api.integration.llm.LlmResponse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Claude through the official Anthropic Java SDK. Reads images and PDFs
 * natively, so the document adapter passes them through untouched.
 *
 * <p>A refusal ({@code stop_reason=refusal}) is surfaced as
 * {@link LlmException.Kind#REFUSED}; the gateway then tries the next provider
 * in {@code llm.fallback-providers}. Anthropic's server-side fallbacks beta is
 * not used here because bill reading is far from the classifier domains and
 * the gateway already has a provider-level fallback chain.
 */
@Slf4j
public class ClaudeLlmProvider implements LlmProvider {

    public static final String TYPE = "claude";
    public static final String DEFAULT_MODEL = "claude-opus-5";

    private final String key;
    private final LlmProperties.Provider config;
    private volatile AnthropicClient client;

    public ClaudeLlmProvider(String key, LlmProperties.Provider config) {
        this.key = key;
        this.config = config;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public LlmProviderInfo info() {
        boolean configured = config.isEnabled() && notBlank(config.getApiKey());
        String label = notBlank(config.getLabel()) ? config.getLabel() : "Claude (Anthropic)";
        return new LlmProviderInfo(key, label, TYPE, model(), null, true, true, configured, config.isFree(), config.getNote());
    }

    private String model() {
        return notBlank(config.getModel()) ? config.getModel().trim() : DEFAULT_MODEL;
    }

    @Override
    public LlmResponse complete(LlmRequest request) {
        if (!info().configured()) {
            throw new LlmException(LlmException.Kind.NOT_CONFIGURED, key,
                    "Claude is not configured: set ANTHROPIC_API_KEY (llm.providers." + key
                            + ".api-key) or choose another provider in AI Settings");
        }

        List<ContentBlockParam> blocks = new ArrayList<>();
        StringBuilder inlineText = new StringBuilder();
        for (LlmAttachment attachment : request.getAttachments()) {
            if (attachment.isImage()) {
                blocks.add(ContentBlockParam.ofImage(ImageBlockParam.builder()
                        .source(Base64ImageSource.builder()
                                .mediaType(mediaType(attachment))
                                .data(attachment.base64())
                                .build())
                        .build()));
            } else if (attachment.isPdf()) {
                blocks.add(ContentBlockParam.ofDocument(DocumentBlockParam.builder()
                        .source(Base64PdfSource.builder().data(attachment.base64()).build())
                        .title(attachment.fileName() == null ? "document.pdf" : attachment.fileName())
                        .build()));
            } else if (attachment.isText()) {
                inlineText.append("\n\n--- Document text (").append(attachment.fileName()).append(") ---\n")
                        .append(attachment.asText());
            } else {
                throw new LlmException(LlmException.Kind.UNSUPPORTED_INPUT, key,
                        "Claude cannot read attachments of type " + attachment.mediaType());
            }
        }
        String prompt = request.getUserPrompt() == null ? "" : request.getUserPrompt();
        if (request.isJsonOutput()) {
            prompt = prompt + "\n\nRespond with a single JSON object and nothing else - no markdown fences, no commentary.";
        }
        blocks.add(ContentBlockParam.ofText(TextBlockParam.builder().text(prompt + inlineText).build()));

        long maxTokens = request.getMaxTokens() != null ? request.getMaxTokens()
                : (config.getMaxTokens() != null ? config.getMaxTokens() : 8192);
        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(model())
                .maxTokens(maxTokens)
                .addUserMessageOfBlockParams(blocks);
        if (notBlank(request.getSystemPrompt())) {
            builder.system(request.getSystemPrompt());
        }
        effort().ifPresent(effort -> builder.outputConfig(OutputConfig.builder().effort(effort).build()));

        long start = System.nanoTime();
        try {
            Message response = client().messages().create(builder.build());
            long latency = (System.nanoTime() - start) / 1_000_000;
            String stop = response.stopReason().isPresent() ? String.valueOf(response.stopReason().get()) : "";
            if (stop.toLowerCase(Locale.ROOT).contains("refusal")) {
                String why = response.stopDetails().map(d -> String.valueOf(d.explanation())).orElse("");
                throw new LlmException(LlmException.Kind.REFUSED, key,
                        "Claude declined this request" + (why.isBlank() ? "" : ": " + why));
            }
            String text = response.content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(TextBlock::text)
                    .collect(Collectors.joining());
            Long inputTokens = response.usage().inputTokens();
            Long outputTokens = response.usage().outputTokens();
            return new LlmResponse(key, response.model().asString(), text, inputTokens, outputTokens, latency, stop);
        } catch (LlmException ex) {
            throw ex;
        } catch (RateLimitException ex) {
            throw new LlmException(LlmException.Kind.RATE_LIMITED, key,
                    "Claude rate limit reached - try again shortly", ex);
        } catch (UnauthorizedException | PermissionDeniedException ex) {
            throw new LlmException(LlmException.Kind.AUTHENTICATION, key,
                    "Claude rejected the API key (" + ex.getClass().getSimpleName() + ") [key used: "
                            + LlmKeyMask.fingerprint(config.getApiKey()) + "]", ex);
        } catch (BadRequestException ex) {
            throw new LlmException(LlmException.Kind.PROVIDER_ERROR, key,
                    "Claude rejected the request: " + ex.getMessage(), ex);
        } catch (InternalServerException ex) {
            throw new LlmException(LlmException.Kind.PROVIDER_ERROR, key,
                    "Claude is temporarily unavailable: " + ex.getMessage(), ex);
        } catch (AnthropicIoException ex) {
            throw new LlmException(LlmException.Kind.TIMEOUT, key,
                    "Could not reach Claude: " + rootMessage(ex), ex);
        } catch (AnthropicServiceException ex) {
            throw new LlmException(LlmException.Kind.PROVIDER_ERROR, key,
                    "Claude error: " + ex.getMessage(), ex);
        } catch (AnthropicException ex) {
            throw new LlmException(LlmException.Kind.PROVIDER_ERROR, key,
                    "Claude client error: " + rootMessage(ex), ex);
        }
    }

    private AnthropicClient client() {
        AnthropicClient existing = client;
        if (existing == null) {
            synchronized (this) {
                if (client == null) {
                    client = AnthropicOkHttpClient.builder()
                            .apiKey(config.getApiKey().trim())
                            .timeout(Duration.ofSeconds(Math.max(30, config.getTimeoutSeconds())))
                            .maxRetries(2)
                            .build();
                }
                existing = client;
            }
        }
        return existing;
    }

    private java.util.Optional<OutputConfig.Effort> effort() {
        if (!notBlank(config.getEffort())) {
            return java.util.Optional.empty();
        }
        switch (config.getEffort().trim().toLowerCase(Locale.ROOT)) {
            case "low":
                return java.util.Optional.of(OutputConfig.Effort.LOW);
            case "medium":
                return java.util.Optional.of(OutputConfig.Effort.MEDIUM);
            case "high":
                return java.util.Optional.of(OutputConfig.Effort.HIGH);
            case "xhigh":
                return java.util.Optional.of(OutputConfig.Effort.XHIGH);
            case "max":
                return java.util.Optional.of(OutputConfig.Effort.MAX);
            default:
                log.warn("Unknown Claude effort '{}' for provider {} - using the API default", config.getEffort(), key);
                return java.util.Optional.empty();
        }
    }

    private Base64ImageSource.MediaType mediaType(LlmAttachment attachment) {
        switch (attachment.normalizedMediaType()) {
            case "image/png":
                return Base64ImageSource.MediaType.IMAGE_PNG;
            case "image/jpeg":
                return Base64ImageSource.MediaType.IMAGE_JPEG;
            case "image/gif":
                return Base64ImageSource.MediaType.IMAGE_GIF;
            case "image/webp":
                return Base64ImageSource.MediaType.IMAGE_WEBP;
            default:
                throw new LlmException(LlmException.Kind.UNSUPPORTED_INPUT, key,
                        "Claude accepts PNG, JPEG, GIF or WEBP images, not " + attachment.mediaType());
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static String rootMessage(Throwable ex) {
        Throwable t = ex;
        while (t.getCause() != null && t.getCause() != t) {
            t = t.getCause();
        }
        return t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
    }
}
