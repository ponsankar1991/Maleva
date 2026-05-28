package my.maleva.api.module.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CommonDto - Common DTO for basic request/response
 * Equivalent to .NET CommonModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonDto {

    private String mobileData;

    private String saleId;

    private Integer comid;

    private Integer id;
}

