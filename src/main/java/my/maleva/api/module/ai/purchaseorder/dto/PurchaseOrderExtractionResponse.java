package my.maleva.api.module.ai.purchaseorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * A pre-filled draft for the Purchase Order form. Ids are resolved against
 * the company's masters where the match is confident; otherwise the raw text
 * and candidate matches are returned so the clerk can pick.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderExtractionResponse {

    private String provider;
    private String model;
    private long latencyMs;
    private Long inputTokens;
    private Long outputTokens;

    private SupplierMatch supplier;
    private Header header;
    private List<Line> lines;
    private List<String> warnings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierMatch {
        private String extractedName;
        private Integer supplierId;
        private double matchConfidence;
        private List<Candidate> candidates;
        private Integer paymentTermsId;
    }

    public record Candidate(Integer id, String name, double score) {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        /** QUOTATION | PROFORMA_INVOICE | INVOICE | DELIVERY_ORDER | PURCHASE_ORDER | OTHER */
        private String documentType;
        private String invoiceNo;
        /** yyyy-MM-dd */
        private String invoiceDate;
        /** yyyy-MM-dd; the PO date, defaults to the document date. */
        private String poDate;
        private String dueDate;
        private String deliveryDate;
        private String currencyCode;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private String paymentTermsText;
        /** Matched to one of the company's past PO descriptions when possible, else the raw text. */
        private String description;
        private String remarks;
        /** The supplier's own reference / PO number printed on the document. */
        private String purchaseOrderNo;
        private String jobNo;
        private String loadingVessel;
        private String offVessel;
        private String vehiclePlateNo;
        private Integer truckId;
        private String truckName;
        private String driverName;
        private Integer driverId;
        private String driverMatchedName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Line {
        private String description;
        private String serialNo;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal taxPercent;
        private BigDecimal taxAmount;
        private BigDecimal amount;
        /** GLAccounts.GLAccountCode when resolved. */
        private String accountCode;
        /** GLAccounts.RowIndex - what the grid stores as ProductRefId / AccountMasterRefId. */
        private Integer accountId;
        private String accountName;
        /** ProductMaster.Id when the line's item code or name matched a store item. */
        private Integer storeItemId;
        private String storeItemCode;
        private String storeItemName;
        private String uom;
    }
}
