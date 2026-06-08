package my.maleva.api.module.qutation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDetailsDto {
    private Integer id;
    private Integer jobMasterRefId;
    private Integer customerMasterRefId;
    private Integer itemMasterRefId;
    private Float mrp;
    private Float purchaseRate;
    private Float landingCost;
    private Float salesRate;
    private String jobName;
    private String productName;
    private String productCode;
    private String customerName;
    private Integer active;
    private Integer isTransport;
    private Integer start;
    private Integer ends;
    private String uom;
    private String port;
}

