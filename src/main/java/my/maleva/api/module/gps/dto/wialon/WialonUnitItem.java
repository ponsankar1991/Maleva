package my.maleva.api.module.gps.dto.wialon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** One {@code avl_unit} - a tracked vehicle in Wialon. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WialonUnitItem {

    @JsonProperty("id")
    private Long id;

    /** Unit name in Wialon; matched against TruckMaster.TruckName. */
    @JsonProperty("nm")
    private String name;

    @JsonProperty("cls")
    private Integer cls;
}
