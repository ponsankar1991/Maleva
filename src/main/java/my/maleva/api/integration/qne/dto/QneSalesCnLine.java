package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

/**
 * QNE sales credit note request line.
 * Legacy: SaleCreditDetailInsertQNE.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneSalesCnLine {
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
    @JsonProperty("WTaxCode")
    private String wTaxCode;
    private String stockLocation;
    private String project;
    private String costCentre;
    private String ref;
    private String ref2;
    private String ref3;
    private String ref4;
    private String ref5;
    private String dateRef1;
    private String dateRef2;
    private double numRef1;
    private double numRef2;
    private long pos;
}
