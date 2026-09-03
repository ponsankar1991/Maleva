package my.maleva.api.module.ai.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Reads a number the way a model might print it: {@code 1234.5},
 * {@code "1,234.50"}, {@code "RM 1,234.50"}, {@code "-"} or {@code null}.
 * Anything without digits becomes null rather than failing the whole parse.
 */
public class LenientDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            return parser.getDecimalValue();
        }
        if (token == JsonToken.VALUE_STRING) {
            return parse(parser.getText());
        }
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        // Objects / arrays in a number slot: skip them and treat as missing.
        parser.skipChildren();
        return null;
    }

    public static BigDecimal parse(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        boolean negative = text.startsWith("(") && text.endsWith(")") || text.contains("-");
        String digits = text.replaceAll("[^0-9.]", "");
        if (digits.isEmpty() || digits.equals(".")) {
            return null;
        }
        // "1.234,50" (European) is rare on Malaysian documents; assume the last dot is the decimal point.
        int lastDot = digits.lastIndexOf('.');
        if (lastDot >= 0) {
            digits = digits.substring(0, lastDot).replace(".", "") + digits.substring(lastDot);
        }
        try {
            BigDecimal value = new BigDecimal(digits);
            return negative ? value.negate() : value;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
