package my.maleva.api.module.pettycash.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** The outcome of a petty cash save. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashSaveResponseDto {

    private boolean success;

    private String message;

    private Integer id;

    /** The running number, e.g. {@code PTC000002451}. */
    private String cNumberDisplay;
}
