package my.maleva.api.integration.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LlmJsonTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void stripsMarkdownFences() {
        String text = "```json\n{\"a\": 1}\n```";
        assertThat(LlmJson.extractJson(text, "p")).isEqualTo("{\"a\": 1}");
    }

    @Test
    void ignoresProseAroundTheObject() {
        String text = "Here is the JSON you asked for:\n{\"a\": {\"b\": [1, 2]}}\nLet me know if you need more.";
        assertThat(LlmJson.extractJson(text, "p")).isEqualTo("{\"a\": {\"b\": [1, 2]}}");
    }

    @Test
    void picksArraysWhenTheyComeFirst() {
        assertThat(LlmJson.extractJson("[1, {\"a\": 2}]", "p")).isEqualTo("[1, {\"a\": 2}]");
    }

    @Test
    void parsesIntoTheRequestedType() {
        Map<?, ?> parsed = LlmJson.parse(mapper, "```\n{\"x\": \"y\"}\n```", Map.class, "p");
        assertThat(parsed.get("x")).isEqualTo("y");
    }

    @Test
    void reportsNonJsonAsBadResponse() {
        assertThatThrownBy(() -> LlmJson.extractJson("I cannot read this document.", "claude"))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("did not return JSON")
                .extracting(ex -> ((LlmException) ex).getKind())
                .isEqualTo(LlmException.Kind.BAD_RESPONSE);
    }

    @Test
    void reportsTruncatedJsonAsBadResponse() {
        assertThatThrownBy(() -> LlmJson.extractJson("{\"a\": [1, 2", "claude"))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("truncated");
    }

    @Test
    void reportsEmptyAnswer() {
        assertThatThrownBy(() -> LlmJson.extractJson("   ", "claude"))
                .isInstanceOf(LlmException.class)
                .hasMessageContaining("empty");
    }
}
