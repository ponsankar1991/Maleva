package my.maleva.api.module.paymentrecept.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptViewBillRequest {

    @JsonAlias({"Id", "id", "customerId", "CustomerId"})
    @JsonProperty("id")
    private Integer id;

    @JsonAlias({"Id2", "id2", "receiptRefId", "ReceiptRefId", "excludeReceiptId"})
    @JsonProperty("id2")
    private Integer id2;

    @JsonAlias({"CompanyRefId", "companyRefId", "Comid", "comid", "companyId", "CompanyId"})
    @JsonProperty("companyRefId")
    private Integer companyRefId;

    @JsonAlias({"tilldate", "TillDate", "tillDate"})
    @JsonProperty("tilldate")
    private String tilldate;

    @JsonAlias({"fromdate", "FromDate", "fromDate"})
    @JsonProperty("fromdate")
    private String fromdate;
}
