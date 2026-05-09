package my.maleva.api.module.billing.billorder.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderDetailsViewDto {
    private Integer id;
    private Integer saleRefId;
    private String productCode;
    private String productName;
    private Float mrp;
    private Float saleRate;
    private Float taxPercent;
    private Float taxAmt;
    private Float discountPercent;
    private Float discountAmt;
    private Float itemQty;
    private Float sAmount;
    private Float quoteValue;
    private String remarksD;
    private String serialNo;
}