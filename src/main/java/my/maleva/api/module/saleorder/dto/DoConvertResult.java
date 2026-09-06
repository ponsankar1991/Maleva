package my.maleva.api.module.saleorder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The outcome of preparing a delivery order from a sale order: the DO's id
 * and number, plus the rows the legacy DO report printed from.
 */
public record DoConvertResult(boolean ok, String message, Integer doId, String doNo, List<DoView> rows) {

    public static DoConvertResult failure(String message) {
        return new DoConvertResult(false, message, null, null, List.of());
    }

    /** One line of the legacy {@code DoViewModel}, aliases kept for the report. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoView {
        @JsonProperty("DoNo")
        private String doNo;
        @JsonProperty("JobNo")
        private String jobNo;
        @JsonProperty("SaleDate")
        private String saleDate;
        @JsonProperty("CustomerName")
        private String customerName;
        @JsonProperty("Address")
        private String address;
        @JsonProperty("AttnName")
        private String attnName;
        @JsonProperty("DODescription")
        private String doDescription;
        @JsonProperty("JobName")
        private String jobName;
        @JsonProperty("AWBNo")
        private String awbNo;
        @JsonProperty("BLCopy")
        private String blCopy;
        @JsonProperty("Offvesselname")
        private String offVesselName;
        @JsonProperty("Loadingvesselname")
        private String loadingVesselName;
        @JsonProperty("TotalWeight")
        private String totalWeight;
        @JsonProperty("Quantity")
        private String quantity;
    }
}
