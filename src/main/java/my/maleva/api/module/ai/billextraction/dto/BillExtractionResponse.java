package my.maleva.api.module.ai.billextraction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * A pre-filled draft for the Bills form. Ids are resolved against the
 * company's masters where the match is confident; otherwise the raw text and
 * candidate matches are returned so the clerk can pick.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillExtractionResponse {

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
        /** Supplier.Id when the match is confident, else null. */
        private Integer supplierId;
        private double matchConfidence;
        private List<Candidate> candidates;
        /** PaymentTermsMaster.Id from the matched supplier or the document's payment terms text. */
        private Integer paymentTermsId;
    }

    public record Candidate(Integer id, String name, double score) {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        private String invoiceNo;
        /** yyyy-MM-dd */
        private String invoiceDate;
        /** yyyy-MM-dd; defaults to the invoice date. */
        private String billDate;
        /** yyyy-MM-dd; from the document, else invoice date + payment terms days, else null. */
        private String dueDate;
        private String currencyCode;
        private BigDecimal subtotal;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;
        private String paymentTermsText;
        /** One of the Bills form's description options, or null. */
        private String description;
        private String remarks;
        private String purchaseOrderNo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Line {
        private String description;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal taxPercent;
        private BigDecimal taxAmount;
        private BigDecimal amount;
        /** GLAccounts.GLAccountCode when resolved. */
        private String accountCode;
        /** GLAccounts.RowIndex - what BillDetails.AccountMasterRefId stores. */
        private Integer accountId;
        private String accountName;
    }
}
