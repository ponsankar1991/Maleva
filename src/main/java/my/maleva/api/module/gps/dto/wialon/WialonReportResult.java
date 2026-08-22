package my.maleva.api.module.gps.dto.wialon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/** The {@code reportResult} body of an executed report. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WialonReportResult {

    @JsonProperty("msgsRendered")
    private Integer msgsRendered;

    /** Name/value pairs such as "Interval beginning" or "Engine hours". */
    @JsonProperty("stats")
    private List<List<String>> stats;

    @JsonProperty("tables")
    private List<WialonReportTable> tables;
}
