package my.maleva.api.integration.myinvois.dto;

import lombok.Data;

/**
 * {@code GET /taxpayer/search/tin} — the TIN LHDN holds for the name and
 * registration number asked about. The only field the endpoint returns.
 */
@Data
public class TaxpayerTinResponse {
    private String tin;
}
