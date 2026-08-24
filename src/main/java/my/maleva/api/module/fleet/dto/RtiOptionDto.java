package my.maleva.api.module.fleet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One option in the levi screen's RTI dropdown.
 *
 * The legacy endpoint returned the shared {@code ComboListModel}, whose fields
 * were named {@code Id} and {@code AccountName} regardless of what they held -
 * here that second field was an RTI number, not an account name.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RtiOptionDto {

    private Integer id;

    /** Printed RTI number, e.g. {@code RTI000000118}. */
    private String rtiNumber;
}
