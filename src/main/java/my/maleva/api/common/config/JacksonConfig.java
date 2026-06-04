package my.maleva.api.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    /**
     * Custom deserializer for Integer fields (wrapper type)
     * ✅ Converts: "" → 0, "14" → 14, null → null
     * Prevents: "value does not contain a character: ''" error from Hibernate
     */
    public static class EmptyStringToIntegerDeserializer extends JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String value = p.getValueAsString();

            // Handle empty strings and whitespace
            if (value == null || value.trim().isEmpty()) {
                return 0;  // ✅ Convert empty string to 0, not null
            }

            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return 0;  // ✅ Invalid number defaults to 0
            }
        }
    }

    /**
     * Custom deserializer for primitive int type
     * ✅ Converts: "" → 0, "14" → 14
     */
    public static class EmptyStringToPrimitiveIntDeserializer extends JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String value = p.getValueAsString();

            if (value == null || value.trim().isEmpty()) {
                return 0;
            }

            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }

    /**
     * Custom deserializer for Double fields (wrapper type)
     * ✅ Converts: "" → 0.0, "3.14" → 3.14, null → null
     */
    public static class EmptyStringToDoubleDeserializer extends JsonDeserializer<Double> {
        @Override
        public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String value = p.getValueAsString();

            // Handle empty strings and whitespace
            if (value == null || value.trim().isEmpty()) {
                return 0.0;  // ✅ Convert empty string to 0.0, not null
            }

            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                return 0.0;  // ✅ Invalid number defaults to 0.0
            }
        }
    }

    /**
     * Custom deserializer for primitive double type
     * ✅ Converts: "" → 0.0, "3.14" → 3.14
     */
    public static class EmptyStringToPrimitiveDoubleDeserializer extends JsonDeserializer<Double> {
        @Override
        public Double deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String value = p.getValueAsString();

            if (value == null || value.trim().isEmpty()) {
                return 0.0;
            }

            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register module for Java 8 Date/Time API (LocalDate, LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Register custom deserializers to handle empty strings
        // ✅ Converts empty strings "" to numeric defaults 0 or 0.0
        // ✅ Handles both wrapper types (Integer, Double) and primitive types (int, double)
        SimpleModule module = new SimpleModule();

        // Register for both wrapper and primitive Integer types
        module.addDeserializer(Integer.class, new EmptyStringToIntegerDeserializer());
        // For primitive int - use the same strategy

        // Register for both wrapper and primitive Double types
        module.addDeserializer(Double.class, new EmptyStringToDoubleDeserializer());
        // For primitive double - use the same strategy

        // Register String deserializer to handle and clean up null strings
        module.addDeserializer(String.class, new StringDeserializer());

        mapper.registerModule(module);

        // Configure to not fail on empty beans and format dates nicely
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        return mapper;
    }

    /**
     * Custom deserializer for String fields
     * ✅ Converts: null → "", "null" → ""
     */
    public static class StringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String value = p.getValueAsString();

            // Handle null and "null" strings
            if (value == null || value.equalsIgnoreCase("null")) {
                return "";  // ✅ Convert null/"null" to empty string
            }

            return value;
        }
    }
}
