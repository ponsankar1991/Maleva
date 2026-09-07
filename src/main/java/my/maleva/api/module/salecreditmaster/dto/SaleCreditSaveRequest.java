package my.maleva.api.module.salecreditmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One credit note as the entry screen submits it — the request shape that
 * replaces the legacy {@code /SaleCredit/InsertSaleCredit} body.
 *
 * <p>Legacy serialised the whole jqxGrid to JSON, string-replaced
 * {@code null} and every apostrophe out of it, and concatenated the result
 * into {@code Exec [SP_SaleCreditMaster] '...'}. A customer named "O'BRIEN"
 * therefore reached the database as "OBRIEN", and the SP re-parsed the text
 * with OPENJSON. This is a typed body instead: one object (the legacy
 * single-element array is still accepted by the controller), bound by Jackson.
 *
 * <p>Only the figures the operator actually enters are read from here —
 * quantity, rate, tax percent and the knock-off amounts. Every derived amount
 * (line tax, line total, header totals) is recomputed on the server, so the
 * stored ledger cannot disagree with what the screen displayed. See
 * {@code SaleCreditEntryServiceImpl}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleCreditSaveRequest {

    /** 0 or null for a new credit note; the existing id to replace one. */
    private Integer id;

    private Integer companyRefId;

    /** AppUser id, validated when non-zero (SP_SaleCreditMaster did the same). */
    private Integer userRefId;

    private Integer customerRefId;

    private Integer employeeRefId;

    /** The invoice this credit note is raised against. The screen requires it. */
    private Integer saleMasterRefId;

    /** yyyy-MM-dd (dd/MM/yyyy is also accepted, as the legacy screen sent). */
    private String saleDate;

    private String remarks;

    /** The customer's currency rate at the time of entry; 0 is refused. */
    private Double currencyValue;

    /** Legacy carried a CStatus on the row; the screen never set anything but 0. */
    private Integer cStatus;

    private List<SaleCreditDetailRequest> saleCreditDetails;

    private List<SaleCreditKnockOffRequest> saleCreditKnockOffDetails;
}
