package my.maleva.api.module.gps.dto.wialon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Response of {@code svc=report/exec_report}. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WialonReportExecResponse {

    @JsonProperty("reportResult")
    private WialonReportResult reportResult;

    @JsonProperty("layerCount")
    private Integer layerCount;
}
