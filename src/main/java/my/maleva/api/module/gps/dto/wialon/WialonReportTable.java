package my.maleva.api.module.gps.dto.wialon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * One result table inside an executed report, e.g. {@code unit_group_fillings},
 * {@code unit_group_speedings} or {@code unit_engine_hours}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WialonReportTable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("label")
    private String label;

    /** Number of rows available; passed back to select_result_rows as the range end. */
    @JsonProperty("rows")
    private Integer rows;

    @JsonProperty("level")
    private Integer level;

    @JsonProperty("columns")
    private Integer columns;

    @JsonProperty("header")
    private List<String> header;
}
