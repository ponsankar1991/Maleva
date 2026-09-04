package my.maleva.api.module.saleorderforwardingreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row of the ZB tab.
 *
 * <p>Unlike the forwarding grid this is one row per sale order, not per leg:
 * the two ZB slots sit side by side as columns rather than being unpivoted.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZbReportRowDto {

    private Integer id;
    private String cNumberDisplay;

    /** Sale date, pre-formatted dd/MM/yyyy for display. */
    private String saleDateDisplay;

    private String zb;
    private String zbRef;
    private String zb2;
    private String zbRef2;
    private String jobType;
}
