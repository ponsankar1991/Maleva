package my.maleva.api.module.gps.dto.wialon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** One report template belonging to an {@code avl_resource}. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WialonReportTemplate {

    @JsonProperty("id")
    private Integer id;

    /** Template name, e.g. "Maleva Group Engine Hours". */
    @JsonProperty("n")
    private String name;

    @JsonProperty("ct")
    private String contentType;
}
