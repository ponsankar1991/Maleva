package my.maleva.api.module.invoice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * One saved invoice, shaped for the entry screen's form.
 *
 * <p>Field names match the React {@code SaleInvoiceFormState} and
 * {@code SaleInvoiceLineItem} exactly, so the screen fills itself without a
 * translation layer guessing at names. Everything is a string where the form
 * holds a string — the inputs are text inputs, and a number arriving as a
 * number turns into "null" or "0" in the box on the first render.
 */
@Data
@Builder
public class SaleInvoiceEditDto {

    private Form form;
    private List<Line> lines;
    /** {@code pickupAddress} split on the "{@}" separator the screen writes. */
    private List<String> pickupList;
    private List<String> deliveryList;
    private Double currencyValue;
    private Integer symbolId;

    /**
     * The header. Only the columns SaleMaster actually has: the loading and
     * off port-charge boxes on the screen (loadingAmt1, offPtwNo, and the
     * rest) exist on the sale order, not on the invoice, so they stay empty
     * here rather than being invented.
     */
    @Data
    @Builder
    public static class Form {
        private Integer id;
        private String invoiceNo;
        /** yyyy-MM-dd, what a date input reads. */
        private String invoiceDate;
        private String customerId;
        private String jobTypeId;
        private String statusId;
        private String saleType;
        private String billType;
        private String description;
        private String remarks;
        private String remarks1;
        private String loadingVesselName;
        private String offVesselName;
        private String awbNo;
        private String ptwNo;
        private String blCopy;
        private String quantity;
        private String weight;
        private String truckSize;
        private String loadingPort;
        private String offPort;
        private String loadingVesselType;
        private String offVesselType;
        private String commodity;
        private String cargo;
        private String loadingScn;
        private String offScn;
        private String pickupAddress;
        private String deliveryAddress;
        private String warehouseAddress;
        private String origin;
        private String destination;
        private String forkliftId;
        private String agentCompanyId;
        private String agentId;
        private String offAgentCompanyId;
        private String offAgentId;
        private String forwarding1;
        private String forwarding2;
        private String forwarding3;
        private String forwardingEnterRef1;
        private String forwardingExitRef1;
        private String forwardingEnterRef2;
        private String forwardingExitRef2;
        private String forwardingEnterRef3;
        private String forwardingExitRef3;
        private String forwardingSmkNo1;
        private String forwardingSmkNo2;
        private String forwardingSmkNo3;
        private String sealBy1;
        private String breakSealBy1;
        private String sealBy2;
        private String breakSealBy2;
        private String sealBy3;
        private String breakSealBy3;
        private String sealAmount1;
        private String breakSealAmount1;
        private String sealAmount2;
        private String breakSealAmount2;
        private String sealAmount3;
        private String breakSealAmount3;
        private String zb1;
        private String zb2;
        private String zbRef1;
        private String zbRef2;
        private String boardingOfficer1;
        private String boardingOfficer2;
        private String boardingAmount1;
        private String boardingAmount2;
        private String portChargesRef;
        private String portCharges;
        /** yyyy-MM-dd'T'HH:mm, what a datetime-local input reads. */
        private String eta;
        private String etb;
        private String etd;
        private String offEta;
        private String offEtb;
        private String offEtd;
        private String pickupDate;
        private String deliveryDate;
        private String warehouseEnterDate;
        private String warehouseExitDate;
    }

    /** One invoice line, named as the grid's row type names them. */
    @Data
    @Builder
    public static class Line {
        private Integer id;
        private String productCode;
        private String productName;
        private String sdRemarks;
        private Integer itemMasterRefId;
        private Double itemQty;
        private Double salesRate;
        private String taxCode;
        private Double taxPercent;
        private Integer taxRefId;
        private Double taxAmount;
        private Double amount;
        private Double currencyValue;
        private Double actualAmount;
        private Integer saleOrderMasterRefId;
        private Integer saleMasterRefId;
        /** The job number of the sale order this line bills, for the grid. */
        private String saleJobNo;
    }
}
