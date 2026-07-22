package my.maleva.api.module.rti.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for complex joined view of RTIMaster
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RTIViewDto {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("CNumberDisplay")
    private String cNumberDisplay;

    @JsonProperty("CNumber")
    private Integer cNumber;

    @JsonProperty("SaleDate")
    private LocalDateTime saleDate;

    @JsonProperty("SealBy")
    private String sealBy;

    @JsonProperty("BreakSealBy")
    private String breakSealBy;

    @JsonProperty("JobNumber")
    private String jobNumber;

    @JsonProperty("EmployeeName")
    private String employeeName;

    @JsonProperty("DriverName")
    private String driverName;

    @JsonProperty("TruckName")
    private String truckName;
}
