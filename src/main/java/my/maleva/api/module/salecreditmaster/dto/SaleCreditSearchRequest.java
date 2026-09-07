package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The filter behind the SALECREDIT ENTRY VIEW grid (legacy F5 window).
 *
 * <p>As in legacy, a non-blank {@code search} is an exact document number and
 * drops every other filter — but unlike legacy it stays scoped to the company.
 * The old WHERE was {@code ... and A.CNumberDisplay='x' or A.SaleMasterRefId=(...)},
 * and SQL's {@code or} binding made the whole company filter optional, so a
 * search could return another company's credit notes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditSearchRequest {

    private Integer companyId;

    /** yyyy-MM-dd or dd/MM/yyyy; defaults to today. */
    private String fromDate;
    private String toDate;

    /** 0 = every customer. */
    private Integer customerId;

    /** 0 = every employee ("Employee Only" unticked). */
    private Integer employeeId;

    /** A credit note number, or the invoice number it was raised against. */
    private String search;
}
