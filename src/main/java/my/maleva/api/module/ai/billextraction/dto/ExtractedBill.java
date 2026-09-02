package my.maleva.api.module.ai.billextraction.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The JSON the model is asked to return. Deliberately loose: every field is
 * optional and numbers tolerate currency symbols, because the service
 * validates and resolves the values afterwards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtractedBill {

    private ExtractedSupplier supplier;
    private String invoiceNo;
    private String invoiceDate;
    private String dueDate;
    private String currencyCode;
    private String paymentTermsText;
    private String purchaseOrderNo;

    @JsonDeserialize(using = LenientDecimalDeserializer.class)
    private BigDecimal subtotal;
    @JsonDeserialize(using = LenientDecimalDeserializer.class)
    private BigDecimal taxAmount;
    @JsonDeserialize(using = LenientDecimalDeserializer.class)
    private BigDecimal discountAmount;
    @JsonDeserialize(using = LenientDecimalDeserializer.class)
    private BigDecimal roundingAdjustment;
    @JsonDeserialize(using = LenientDecimalDeserializer.class)
    private BigDecimal totalAmount;

    private String descriptionCategory;

    @Builder.Default
    private List<ExtractedLine> lines = new ArrayList<>();

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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExtractedLine {
        private String description;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal quantity;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal unitPrice;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal taxPercent;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal taxAmount;
        @JsonDeserialize(using = LenientDecimalDeserializer.class)
        private BigDecimal amount;
        private String accountCode;
    }
}
