package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegacyFwDataDto {
    @JsonProperty("IsSuccess")
    private Boolean isSuccess;
    
    @JsonProperty("StatusCode")
    private Integer statusCode;
    
    @JsonProperty("Message")
    private String message;
    
    @JsonProperty("Data1")
    private List<FwReportModel> data1;
    
    @JsonProperty("Data2")
    private List<FwModel> data2;
    
    @JsonProperty("Data3")
    private Object data3;
    @JsonProperty("Data4")
    private Object data4;
    @JsonProperty("Data5")
    private Object data5;
    @JsonProperty("Data6")
    private Object data6;
    @JsonProperty("Data7")
    private Object data7;
    @JsonProperty("Data8")
    private Object data8;
    @JsonProperty("Data9")
    private Object data9;
    @JsonProperty("Data10")
    private Object data10;
    @JsonProperty("Data11")
    private Object data11;

    @Data
    @NoArgsConstructor
    public static class FwReportModel {
        @JsonProperty("TodayCount")
        private Integer todayCount = 0;
        @JsonProperty("TodayRelease")
        private Integer todayRelease = 0;
        @JsonProperty("TodayWithRelease")
        private Integer todayWithRelease = 0;
        @JsonProperty("YesterdayCount")
        private Integer yesterdayCount = 0;
        @JsonProperty("YesterdayRelease")
        private Integer yesterdayRelease = 0;
        @JsonProperty("YesterdayWithRelease")
        private Integer yesterdayWithRelease = 0;
        @JsonProperty("WeekCount")
        private Integer weekCount = 0;
        @JsonProperty("WeekRelease")
        private Integer weekRelease = 0;
        @JsonProperty("WeekWithRelease")
        private Integer weekWithRelease = 0;
        @JsonProperty("MonthCount")
        private Integer monthCount = 0;
        @JsonProperty("MonthRelease")
        private Integer monthRelease = 0;
        @JsonProperty("MonthWithRelease")
        private Integer monthWithRelease = 0;
    }

    @Data
    @NoArgsConstructor
    public static class FwModel {
        @JsonProperty("K1Count")
        private Integer k1Count = 0;
        @JsonProperty("K1Release")
        private Integer k1Release = 0;
        @JsonProperty("K1WithRelease")
        private Integer k1WithRelease = 0;
        @JsonProperty("K2Count")
        private Integer k2Count = 0;
        @JsonProperty("K2Release")
        private Integer k2Release = 0;
        @JsonProperty("K2WithRelease")
        private Integer k2WithRelease = 0;
        @JsonProperty("K3Count")
        private Integer k3Count = 0;
        @JsonProperty("K3Release")
        private Integer k3Release = 0;
        @JsonProperty("K3WithRelease")
        private Integer k3WithRelease = 0;
        @JsonProperty("K8Count")
        private Integer k8Count = 0;
        @JsonProperty("K8Release")
        private Integer k8Release = 0;
        @JsonProperty("K8WithRelease")
        private Integer k8WithRelease = 0;
    }
}
