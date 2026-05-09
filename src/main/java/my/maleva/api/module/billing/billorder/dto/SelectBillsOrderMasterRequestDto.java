package my.maleva.api.module.billing.billorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectBillsOrderMasterRequestDto {

    @JsonProperty("Comid")
    @NotNull(message = "Company ID is required")
    @Positive(message = "Company ID must be positive")
    private Integer comid;

    @JsonProperty("BillId")
    private Integer billId;

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("TId")
    private Integer tId;

    @JsonProperty("DId")
    private Integer dId;

    @JsonProperty("Employeeid")
    private Integer employeeid;

    @JsonProperty("status")
    private String status;

    @JsonProperty("Offvesselname")
    private String offvesselname;

    @JsonProperty("Search")
    private String search;

    @JsonProperty("Remarks")
    private Integer remarks;

    @JsonProperty("VessalNameSearch")
    private String vessalNameSearch;

    @JsonProperty("Fromdate")
    private String fromdate;

    @JsonProperty("Todate")
    private String todate;

    @JsonProperty("invoicecheck")
    private Integer invoicecheck;
}