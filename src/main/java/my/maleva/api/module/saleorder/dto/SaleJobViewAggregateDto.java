package my.maleva.api.module.saleorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleJobViewAggregateDto {

    @JsonProperty("CurrencyName")
    private String currencyName;

    @JsonProperty("CountryName")
    private String countryName;

    @JsonProperty("JobCount")
    private Integer jobCount;

    @JsonProperty("EmployeeName")
    private String employeeName;

    @JsonProperty("EmployeeCount")
    private Integer employeeCount;

    @JsonProperty("JobType")
    private String jobType;

    @JsonProperty("TypeCount")
    private Integer typeCount;

    @JsonProperty("JobStatus")
    private String jobStatus;

    @JsonProperty("StatusCount")
    private Integer statusCount;

    @JsonProperty("CustomerName")
    private String customerName;

    @JsonProperty("Month1")
    private Double month1;

    @JsonProperty("Month2")
    private Double month2;

    @JsonProperty("Month3")
    private Double month3;

    @JsonProperty("CurrentMonth")
    private Double currentMonth;

    @JsonProperty("Amount")
    private Double amount;

    @JsonProperty("CompanyRefId")
    private Integer companyRefId;
}
