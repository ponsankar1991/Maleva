package my.maleva.api.module.saleorderforwardingreport.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The six S1/S2 filter dropdowns, in one response.
 *
 * <p>Legacy `SelectComboS1` ran six near-identical `SELECT DISTINCT` queries and
 * returned them as `Data1`..`Data6`, leaving the caller to remember which
 * position meant which column. They are named here instead.
 *
 * <p>Each list holds the distinct, trimmed, non-blank values that column
 * actually holds for the company — these are free-text fields, so the options
 * are whatever has been typed before.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForwardingS1OptionsDto {

    private List<String> forwarding1S1;
    private List<String> forwarding1S2;
    private List<String> forwarding2S1;
    private List<String> forwarding2S2;
    private List<String> forwarding3S1;
    private List<String> forwarding3S2;
}
