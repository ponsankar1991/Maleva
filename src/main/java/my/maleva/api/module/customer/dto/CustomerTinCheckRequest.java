package my.maleva.api.module.customer.dto;

import lombok.Data;

/**
 * What the customer screen's TIN button asks about. Field names match the
 * legacy payload so the two systems' logs line up.
 */
@Data
public class CustomerTinCheckRequest {

    /** The TIN on the form. Filled means validate; blank means search. */
    private String customerTin;

    /** The registration number — BRN, NRIC, passport or army number. */
    private String idValue;

    /** The customer name as LHDN holds it. */
    private String taxpayerName;

    /**
     * Legacy carried this and the customer screen never sent it; kept so the
     * contract is the same one, and so a caller that does send it still works.
     */
    private String fileType;
}
