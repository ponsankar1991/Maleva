package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * QNE sales invoice detail line (request).
 * Legacy: SaleInvoiceDetailQneInsertModel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneSalesInvoiceLine {

    private String numbering;
    private String stock;
    private String description;
    private String note;
    private String uom;
    private double qty;
    private String taxCode;
    @JsonProperty("IsTaxInclusive")
    private boolean isTaxInclusive;
    private double unitPrice;
    private String discount;
    private String referenceNo;
    private String glAccount;
    private String project;
    private String costCentre;
    @JsonProperty("WTaxCode")
    private String wTaxCode;
    private String ref;
    private String ref2;
    private String ref3;
    private String ref4;
    private String ref5;
    private String dateRef1;
    private String dateRef2;
    private double numRef1;
    private double numRef2;
    private String stockLocation;
    @Builder.Default
    private QneTransferFrom transferFrom = new QneTransferFrom();
}
