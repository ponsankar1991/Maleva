package my.maleva.api.module.paymentrecept.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filters of the RECEIPT ENTRY VIEW grid — the port of the legacy
 * {@code SelectReceipt(Comid, Fromdate, Todate, SId, Employeeid, Search)}.
 *
 * <p>Dates are ISO {@code yyyy-MM-dd}. A non-blank {@code search} is an exact
 * receipt number and, as in legacy, drops every other filter including the
 * dates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptSearchRequest {

    @JsonAlias({"Comid", "comid", "companyRefId", "CompanyRefId", "CompanyId"})
    private Integer companyId;

    @JsonAlias({"Fromdate", "fromdate", "FromDate"})
    private String fromDate;

    @JsonAlias({"Todate", "todate", "ToDate"})
    private String toDate;

    /** 0 = every customer. */
    @JsonAlias({"SId", "sId", "CustomerId", "customerRefId"})
    private Integer customerId;

    /** 0 = every clerk. */
    @JsonAlias({"Employeeid", "employeeid", "EmployeeId", "employeeRefId"})
    private Integer employeeId;

    @JsonAlias({"Search"})
    private String search;
}
