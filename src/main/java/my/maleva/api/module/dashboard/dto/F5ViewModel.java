package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * F5ViewModel - Search model for Invoice check operations
 * Converted from C# CheckSaleInvoiceCount method
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class F5ViewModel {

    @JsonProperty("comId")
    private Integer comId;

    @JsonProperty("invoice")
    private Boolean invoice; // true for invoice checks

    @JsonProperty("fromDate")
    private String fromDate;

    @JsonProperty("toDate")
    private String toDate;

    @JsonProperty("remarks")
    private Integer remarks; // 1=With Invoice, 2=Without Invoice

    @JsonProperty("statusId")
    private Integer statusId;

    @JsonProperty("employeeId")
    private Integer employeeId;

    @JsonProperty("completeStatusNotShow")
    private Boolean completeStatusNotShow;
}


