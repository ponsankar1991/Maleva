package my.maleva.api.module.saleorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderQuickUpdateDto {
    private Integer id;
    private Integer companyRefId;
    private Integer jStatus;
    private String statusName;
    private String cNumberDisplay;
    private String eta;
    private String etb;
    private String oeta;
    private String oetb;
    private String seta;
    private String setb;
    private String soeta;
    private String soetb;
}
