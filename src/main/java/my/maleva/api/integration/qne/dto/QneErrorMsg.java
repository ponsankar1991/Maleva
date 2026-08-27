package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * QNE's error envelope, mirroring the legacy {@code ErrorMsg} model.
 *
 * <p>The casing is QNE's, not ours: {@code code} is lower-case and
 * {@code Message} is Pascal-case on the wire, exactly as the .NET model had
 * them. Unknown properties are ignored because QNE decorates errors with
 * fields the legacy contract never read.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class QneErrorMsg {

    @JsonProperty("code")
    private String code;

    @JsonProperty("Message")
    private String message;
}
