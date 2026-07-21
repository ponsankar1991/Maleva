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
public class SaleJobViewAggregateDto {

    @JsonProperty("CurrencyName")
    private String currencyName;

    @JsonProperty("CountryName")
    private String countryName;

    @JsonProperty("JobCount")
    @Builder.Default
    private Integer jobCount = 0;

    @JsonProperty("EmployeeName")
    private String employeeName;

    @JsonProperty("EmployeeCount")
    @Builder.Default
    private Integer employeeCount = 0;

    @JsonProperty("JobType")
    private String jobType;

    @JsonProperty("TypeCount")
    @Builder.Default
    private Integer typeCount = 0;

    @JsonProperty("JobStatus")
    private String jobStatus;

    @JsonProperty("StatusCount")
    @Builder.Default
    private Integer statusCount = 0;

    @JsonProperty("CustomerName")
    private String customerName;

    @JsonProperty("Month1")
    private String month1;

    @JsonProperty("Month2")
    private String month2;

    @JsonProperty("Month3")
    private String month3;

    @JsonProperty("CurrentMonth")
    private String currentMonth;

    @JsonProperty("Amount")
    @Builder.Default
    private Integer amount = 0;

    @JsonProperty("CompanyRefId")
    @Builder.Default
    private Integer companyRefId = 0;
}
