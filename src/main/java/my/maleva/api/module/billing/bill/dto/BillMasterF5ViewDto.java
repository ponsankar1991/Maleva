package my.maleva.api.module.billing.bill.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The bill F5 search result: header rows plus every line belonging to them,
 * flat. The grid nests them client-side by matching detail.saleRefId to
 * billMaster.id — the same shape the legacy screen consumed.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillMasterF5ViewDto {

    private List<BillMasterViewDto> billMaster;
    private List<BillDetailsViewDto> billDetails;
}
