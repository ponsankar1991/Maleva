package my.maleva.api.module.gps.dto.wialon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * One cell of a report row. {@code t} carries the rendered text, which is what
 * the legacy parser used; {@code y}/{@code x} carry latitude/longitude when the
 * cell is a location.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WialonResultCell {

    /** Rendered text value. */
    @JsonProperty("t")
    private String text;

    /** Raw value - double, int or string depending on the column. */
    @JsonProperty("v")
    private Object rawValue;

    @JsonProperty("y")
    private Double latitude;

    @JsonProperty("x")
    private Double longitude;
}
