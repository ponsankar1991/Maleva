package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Intentionally empty object. Legacy sent TransferFrom as a property-less
 * {@code {}} on every sales invoice line; QNE requires the key to exist.
 *
 * <p>The {@link JsonAnyGetter} gives Jackson a (permanently empty) property
 * source, so the class serialises as {@code {}} under any mapper — a plain
 * empty bean would throw {@code FAIL_ON_EMPTY_BEANS} on mappers that keep
 * Jackson's default.
 */
@Data
@NoArgsConstructor
public class QneTransferFrom {

    @JsonAnyGetter
    public Map<String, Object> properties() {
        return Map.of();
    }
}
