package my.maleva.api.module.paymentrecept.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * What the operator confirmed in the SEND RECEIPT window — the port of legacy
 * {@code ReceiptMailModel}. Addresses may be given as a list or as one
 * comma/semicolon separated string in each of {@code to} and {@code cc}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptMailRequest {

    @JsonAlias({"Emailids", "emailids", "To"})
    private List<String> to;

    @JsonAlias({"CcEmailids", "ccEmailids", "Cc"})
    private List<String> cc;

    /** Blank keeps the configured default subject. */
    @JsonAlias({"Subject"})
    private String subject;

    /** Shown in the mail body under the receipt table. */
    @JsonAlias({"Remarks"})
    private String remarks;

    /** Also attach the files stored against the receipt (default false: voucher PDF only). */
    @JsonAlias({"IncludeAttachments"})
    private Boolean includeAttachments;
}
