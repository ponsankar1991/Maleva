package my.maleva.api.module.gps.dto.wialon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Response of {@code svc=token/login}. The {@code eid} field is the session id
 * ("sid") that every subsequent call must carry.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WialonSession {

    @JsonProperty("eid")
    private String eid;

    @JsonProperty("host")
    private String host;

    @JsonProperty("base_url")
    private String baseUrl;

    @JsonProperty("tm")
    private Long tm;

    @JsonProperty("wsdk_version")
    private String wsdkVersion;
}
