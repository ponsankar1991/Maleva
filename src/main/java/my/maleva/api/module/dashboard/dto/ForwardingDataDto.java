package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for forwarding data response.
 *
 * <p>Maps to the legacy {@code POST /Login/GetFWData} response, which carried two
 * result sets that the Forwarding dashboard read side by side:
 * <ul>
 *   <li>{@code Data1} - the period block (Today / Yesterday / Week / Month), produced by
 *       {@code EXEC RT_ForwardingReport @CompanyRefId}. It takes no date range: the SP
 *       derives its own windows from {@code GETDATE()}, so the screen's date pickers do
 *       <b>not</b> move these numbers. That is legacy behaviour, kept deliberately.</li>
 *   <li>{@code Data2} - the K1/K2/K3/K8 block, which <i>is</i> filtered by the date range.</li>
 * </ul>
 * Both are flattened into this one DTO; the field names keep the legacy labels, where
 * "Release" means "no exit ref yet" and "WithRelease" means "exit ref present".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForwardingDataDto {

    // ----- Data1: RT_ForwardingReport period block (ignores fromDate/toDate) -----

    @JsonProperty("todayCount")
    private Integer todayCount;

    @JsonProperty("todayRelease")
    private Integer todayRelease;

    @JsonProperty("todayWithRelease")
    private Integer todayWithRelease;

    @JsonProperty("yesterdayCount")
    private Integer yesterdayCount;

    @JsonProperty("yesterdayRelease")
    private Integer yesterdayRelease;

    @JsonProperty("yesterdayWithRelease")
    private Integer yesterdayWithRelease;

    @JsonProperty("weekCount")
    private Integer weekCount;

    @JsonProperty("weekRelease")
    private Integer weekRelease;

    @JsonProperty("weekWithRelease")
    private Integer weekWithRelease;

    @JsonProperty("monthCount")
    private Integer monthCount;

    @JsonProperty("monthRelease")
    private Integer monthRelease;

    @JsonProperty("monthWithRelease")
    private Integer monthWithRelease;

    // ----- Data2: K1/K2/K3/K8 block (filtered by fromDate/toDate) -----

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
