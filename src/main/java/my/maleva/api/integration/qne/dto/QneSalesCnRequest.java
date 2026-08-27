package my.maleva.api.integration.qne.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.List;

/**
 * QNE sales credit note request master.
 * Legacy: SaleCreditMasterInsertQNE.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class QneSalesCnRequest {
    private String customer;
    private String cnDate;
    private String cnCode;
    private String customerName;
    private String deliveryTerm;
    private String term;
    private String stockLocation;
    private String attention;
    private String salesPerson;
    private String ourDono;
    private String project;
    private String costCentre;
    private double currencyRate;
    private String referenceNo;
    @JsonProperty("IsRounding")
    private boolean isRounding;
    private String phone;
    private String fax;
    private String address1;
    private String address2;
    private String address3;
    private String address4;
    private long termId;
    private long salesPersonId;
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
    @JsonProperty("IsCancelled")
    private boolean isCancelled;
    private String doBranchCode;
    private String doBranchName;
    private String doContact;
    private String doPhone;
    private String doFax;
    private String doAddress1;
    private String doAddress2;
    private String doAddress3;
    private String doAddress4;
    private String discount;
    private String notes;
    @JsonProperty("IsTaxInclusive")
    private boolean isTaxInclusive;
    private String taxDate;
    /** Legacy misspelling on the wire — must stay "DoRegistationNo". */
    private String doRegistationNo;
    private String doGstRegNo;
    private String doPhone2;
    private String doEmail;
    private String doRemark;
    private String deliveryArea;
    @JsonProperty("IsApproved")
    private boolean isApproved;
    private List<QneSalesCnLine> details;
}
