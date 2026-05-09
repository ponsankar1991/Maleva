package my.maleva.api.module.transaction.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    @JsonProperty("Fromdate")
    private LocalDate fromdate;

    @JsonProperty("Todate")
    private LocalDate todate;

    @JsonProperty("CustomerId")
    private Integer customerId;

    @JsonProperty("Comid")
    private Integer comid;

    @JsonProperty("SPort")
    private String sPort;

    @JsonProperty("Search")
    private String search;

    @JsonProperty("Expdate")
    private String expdate;

    @JsonProperty("SFromDate")
    private String sFromDate;

    @JsonProperty("ExpApadBonam")
    private String expApadBonam;

    @JsonProperty("DeliveryDone")
    private Boolean deliveryDone;

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("Jobid")
    private Integer jobid;

    @JsonProperty("ETA")
    private Boolean eta;

    @JsonProperty("Cons")
    private Boolean cons;

    @JsonProperty("ETAType")
    private Integer etaType;

    @JsonProperty("Pickupdate")
    private Boolean pickupdate;

}
