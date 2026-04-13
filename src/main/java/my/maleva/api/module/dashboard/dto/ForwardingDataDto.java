package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for forwarding data response (K1/K2/K3/K8)
 * Maps to legacy GetFWData API response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForwardingDataDto {

    @JsonProperty("k1Count")
    private Integer k1Count;

    @JsonProperty("k1Release")
    private Integer k1Release;

    @JsonProperty("k1WithRelease")
    private Integer k1WithRelease;

    @JsonProperty("k2Count")
    private Integer k2Count;

    @JsonProperty("k2Release")
    private Integer k2Release;

    @JsonProperty("k2WithRelease")
    private Integer k2WithRelease;

    @JsonProperty("k3Count")
    private Integer k3Count;

    @JsonProperty("k3Release")
    private Integer k3Release;

    @JsonProperty("k3WithRelease")
    private Integer k3WithRelease;

    @JsonProperty("k8Count")
    private Integer k8Count;

    @JsonProperty("k8Release")
    private Integer k8Release;

    @JsonProperty("k8WithRelease")
    private Integer k8WithRelease;

    @JsonProperty("report")
    private List<ForwardingReportDto> report;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForwardingReportDto {
        @JsonProperty("Forwarding")
        private String forwarding;

        @JsonProperty("ForwardingNo")
        private String forwardingNo;

        @JsonProperty("JobNo")
        private String jobNo;

        @JsonProperty("CustomerName")
        private String customerName;

        @JsonProperty("ForwardingDate")
        private String forwardingDate;

        @JsonProperty("ExitRef")
        private String exitRef;

        @JsonProperty("ForwardingStatus")
        private String forwardingStatus;
    }
}
