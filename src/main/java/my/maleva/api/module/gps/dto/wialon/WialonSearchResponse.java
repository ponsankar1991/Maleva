package my.maleva.api.module.gps.dto.wialon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Response of {@code svc=core/search_items}, generic over the item type so the
 * same shape serves both the avl_unit and the avl_resource search.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WialonSearchResponse<T> {

    @JsonProperty("totalItemsCount")
    private Integer totalItemsCount;

    @JsonProperty("items")
    private List<T> items;
}
