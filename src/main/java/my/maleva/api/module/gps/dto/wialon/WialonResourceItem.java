package my.maleva.api.module.gps.dto.wialon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/** One {@code avl_resource}, which owns the report templates. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WialonResourceItem {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("nm")
    private String name;

    /** Report templates keyed by template id (as a string). */
    @JsonProperty("rep")
    private Map<String, WialonReportTemplate> reports;
}
