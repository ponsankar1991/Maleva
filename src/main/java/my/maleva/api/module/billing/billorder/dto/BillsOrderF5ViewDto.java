package my.maleva.api.module.billing.billorder.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderF5ViewDto {
    private List<BillsOrderMasterViewDto> billsOrderMaster;
    private List<BillsOrderDetailsViewDto> billsOrderDetails;
}