package my.maleva.api.module.saleorder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Aggregate edit payload for loading one sale order together with its child
 * rows in a single request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderEditDto {

    private SaleOrderMasterDto saleOrderMaster;

    private List<SaleOrderEditDetailsDto> saleOrderDetails;

    private List<SaleOrderPickupDto> pickupDetails;

    private List<SaleOrderDeliveryDto> deliveryDetails;

    private List<SaleOrderForwardingDto> forwardingDetails;

    @JsonProperty("Id")
    public Integer getId() {
        return saleOrderMaster != null ? saleOrderMaster.getId() : null;
    }

    @JsonProperty("CustomerRefId")
    public Integer getCustomerRefId() {
        return saleOrderMaster != null ? saleOrderMaster.getCustomerRefId() : null;
    }

    @JsonProperty("customerRefId")
    public Integer getCustomerRefIdValue() {
        return saleOrderMaster != null ? saleOrderMaster.getCustomerRefId() : null;
    }

    @JsonProperty("CustomerName")
    public String getLegacyCustomerName() {
        return saleOrderMaster != null ? saleOrderMaster.getCustomerName() : null;
    }

    @JsonProperty("customerName")
    public String getCustomerNameValue() {
        return saleOrderMaster != null ? saleOrderMaster.getCustomerName() : null;
    }

    @JsonProperty("JobMasterRefId")
    public Integer getJobMasterRefId() {
        return saleOrderMaster != null ? saleOrderMaster.getJobMasterRefId() : null;
    }

    @JsonProperty("jobMasterRefId")
    public Integer getJobMasterRefIdValue() {
        return saleOrderMaster != null ? saleOrderMaster.getJobMasterRefId() : null;
    }

    @JsonProperty("JStatus")
    public Integer getJStatus() {
        return saleOrderMaster != null ? saleOrderMaster.getJStatus() : null;
    }

    @JsonProperty("jStatus")
    public Integer getJStatusValue() {
        return saleOrderMaster != null ? saleOrderMaster.getJStatus() : null;
    }

    @JsonProperty("StatusName")
    public String getStatusName() {
        return saleOrderMaster != null ? saleOrderMaster.getStatusName() : null;
    }

    @JsonProperty("statusName")
    public String getStatusNameValue() {
        return saleOrderMaster != null ? saleOrderMaster.getStatusName() : null;
    }

    @JsonProperty("JobStatus")
    public String getJobStatus() {
        return saleOrderMaster != null ? saleOrderMaster.getStatusName() : null;
    }

    @JsonProperty("jobStatus")
    public String getJobStatusValue() {
        return saleOrderMaster != null ? saleOrderMaster.getStatusName() : null;
    }

    @JsonProperty("CNumber")
    public Integer getCNumber() {
        return saleOrderMaster != null ? saleOrderMaster.getCNumber() : null;
    }

    @JsonProperty("cNumber")
    public Integer getCNumberValue() {
        return saleOrderMaster != null ? saleOrderMaster.getCNumber() : null;
    }

    @JsonProperty("CNumberDisplay")
    public String getCNumberDisplay() {
        return saleOrderMaster != null ? saleOrderMaster.getCNumberDisplay() : null;
    }

    @JsonProperty("cNumberDisplay")
    public String getCNumberDisplayValue() {
        return saleOrderMaster != null ? saleOrderMaster.getCNumberDisplay() : null;
    }

    @JsonProperty("BillNoDisplay")
    public String getBillNoDisplay() {
        return saleOrderMaster != null ? saleOrderMaster.getCNumberDisplay() : null;
    }

    @JsonProperty("billNoDisplay")
    public String getBillNoDisplayValue() {
        return saleOrderMaster != null ? saleOrderMaster.getCNumberDisplay() : null;
    }
}
