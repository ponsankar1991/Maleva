package my.maleva.api.module.ai.purchaseorder.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.maleva.api.module.ai.common.LenientDecimalDeserializer;
import my.maleva.api.module.ai.common.SupplierHint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The JSON the model returns for a supplier document that becomes a purchase
 * order: a quotation, proforma, invoice or delivery order. Every field is
 * optional; the service validates and resolves afterwards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtractedPurchaseOrder {

    private String documentType;
    private ExtractedSupplier supplier;
    private String documentNo;
    private String documentDate;
    private String orderDate;
    private String dueDate;
    private String deliveryDate;
    private String purchaseOrderNo;
    private String currencyCode;
    private String paymentTermsText;
    private String jobNo;
    private String vehiclePlateNo;
    private String driverName;
    private String loadingVessel;
    private String offVessel;
    private String descriptionCategory;

    @JsonDeserialize(using = LenientDecimalDeserializer.class)
    private BigDecimal subtotal;
    @JsonDeserialize(using = LenientDecimalDeserializer.class)
    private BigDecimal discountAmount;
    @JsonDeserialize(using = LenientDecimalDeserializer.class)
    private BigDecimal taxAmount;
    @JsonDeserialize(using = LenientDecimalDeserializer.class)
    private BigDecimal roundingAdjustment;
    @JsonDeserialize(using = LenientDecimalDeserializer.class)
    private BigDecimal totalAmount;

    @Builder.Default
    private List<ExtractedLine> lines = new ArrayList<>();

    private String remarks;
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractedSupplier {
        private String name;
        private String registrationNo;
        private String gstNo;
        private String sstNo;
        private String tinNo;
        private String address;
        private String phone;
        private String email;

        public SupplierHint toHint() {
            return new SupplierHint(name, registrationNo, gstNo, sstNo, tinNo);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractedLine {
        private String description;
        private String itemCode;
        private String itemName;
        private String serialNo;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal quantity;
        private String uom;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal unitPrice;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal discountPercent;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal discountAmount;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal taxPercent;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal taxAmount;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal amount;
        private String accountCode;
        private String remarks;
    }
}
