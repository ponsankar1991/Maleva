package my.maleva.api.module.saleorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderStatusUpdateDto {
    private Integer id;
    private Integer companyRefId;
    private Integer jStatus;
    private String statusName;
    private String cNumberDisplay;
}
