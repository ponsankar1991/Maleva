package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

/**
 * QNE sales invoice master (request).
 * Legacy: SaleInvoiceMasterQneInsertModel.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneSalesInvoiceRequest {

    private String id;
    private String customer;
    private String invoiceDate;
    private String invoiceCode;
    private String invoiceTo;
    private String deliveryTerm;
    private String term;
    private String stockLocation;
    private String attention;
    private String phone;
    private String fax;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private String referenceNo;
    private String notes;
    private String salesPerson;
    private String ourDono;
    private String title;
    private String title2;
    private String ref1;
    private String ref2;
    private String ref3;
    private String ref4;
    private String ref5;
    private String remark1;
    private String remark2;
    private String remark3;
    private String remark4;
    private String remark5;
    private String project;
    private String costCentre;
    private double currencyRate;
    @JsonProperty("IsTaxInclusive")
    private boolean isTaxInclusive;
    @JsonProperty("IsRounding")
    private boolean isRounding;
    private String doBranchCode;
    private String doBranchName;
    private String doContact;
    private String doPhone;
    private String doAddress1;
    private String doAddress2;
    private String doAddress3;
    private String doAddress4;
    private List<QneSalesInvoiceLine> details;
}
