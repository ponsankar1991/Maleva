package my.maleva.api.module.gps.dto.wialon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * One row returned by {@code svc=report/select_result_rows}.
 *
 * Rows form a tree. {@code d} is the number of child rows at the next nesting
 * level, so {@code d == 0} is a leaf carrying real cell values and anything else
 * is a grouping row whose children sit in {@code r}.
 * See {@link my.maleva.api.module.gps.client.WialonRowMapper}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WialonResultRow {

    @JsonProperty("n")
    private Integer index;

    /** Number of child rows at the next nesting level; 0 means this is a leaf. */
    @JsonProperty("d")
    private Integer childCount;

    @JsonProperty("t1")
    private Long fromTime;

    @JsonProperty("t2")
    private Long toTime;

    /** Cell values of this row. */
    @JsonProperty("c")
    private List<WialonResultCell> cells;

    /** Child rows when this is a grouping row. */
    @JsonProperty("r")
    private List<WialonResultRow> children;
}
