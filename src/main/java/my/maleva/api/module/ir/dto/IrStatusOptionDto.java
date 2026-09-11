package my.maleva.api.module.ir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** One row of the status dropdown. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrStatusOptionDto {

    private Integer id;
    private String statusCode;
    private String statusName;
    private String colorCode;

    /** True when this status ends the workflow, so the grid can grey the row. */
    private boolean finished;
}
