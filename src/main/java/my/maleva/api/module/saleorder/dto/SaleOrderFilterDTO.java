package my.maleva.api.module.saleorder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * SaleOrderFilterDTO - Filter DTO for SelectSaleOrder API
 * Mirrors the .NET F5ViewModel with all filtering parameters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderFilterDTO {

    // The controller marks the body @Valid but nothing on this class was ever
    // constrained, so the annotation did nothing; the company check lived only in
    // SaleOrderFilterHelper. Constrained here so a missing tenant is a 400 with a
    // field name rather than a generic message thrown from the service.
    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be greater than zero")
    @JsonProperty("Comid")
    private Integer comid;

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("JId")
    private Integer jId;

    @JsonProperty("TId")
    private Integer tId;

    @JsonProperty("Employeeid")
    private Integer employeeid;

    @JsonProperty("DashboardStatus")
    private Integer dashboardStatus;

    @JsonProperty("statusList")
    private String statusList;

    @JsonProperty("Statusid")
    private Integer statusid;

    @JsonProperty("completestatusnotshow")
    private Boolean completestatusnotshow;

    @JsonProperty("Remarks")
    private Integer remarks;

    @JsonProperty("Offvesselname")
    private String offvesselname;

    @JsonProperty("Loadingvesselname")
    private String loadingvesselname;

    @JsonProperty("Search")
    private String search;

    @JsonProperty("Invoice")
    private Boolean invoice;

    @JsonProperty("ETA")
    private Boolean eta;

    @JsonProperty("ETAType")
    private Integer etaType;

    @JsonProperty("Fromdate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private LocalDate fromdate;

    @JsonProperty("Todate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private LocalDate todate;

    @JsonProperty("Pickup")
    private Boolean pickup;

    @JsonProperty("PortName")
    private String portName;

    @JsonProperty("Invoicecheck")
    private Boolean invoicecheck;
}
