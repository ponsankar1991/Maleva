package my.maleva.api.module.invoice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiInvoiceDto {

    @JsonProperty("Id")
    private List<Integer> id;

    @JsonProperty("Comid")
    private Integer comid;
}
