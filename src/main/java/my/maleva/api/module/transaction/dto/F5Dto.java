package my.maleva.api.module.transaction.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class F5Dto {
    @JsonProperty("Fromdate")
    @JsonAlias({"fromDate", "FromDate"})
    private LocalDate fromdate;

    @JsonProperty("Todate")
    @JsonAlias({"toDate", "ToDate"})
    private LocalDate todate;

    @JsonProperty("Comid")
    @JsonAlias({"comId", "ComId"})
    private Integer comid;

    @JsonProperty("Employeeid")
    @JsonAlias({"employeeId", "EmployeeId"})
    private Integer employeeId;

    @JsonProperty("Search")
    @JsonAlias({"search"})
    private String search;

    // Optional pagination
    private Integer pageNumber;
    private Integer pageSize;
}
