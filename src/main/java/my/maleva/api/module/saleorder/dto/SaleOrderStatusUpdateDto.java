package my.maleva.api.module.saleorder.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SaleOrderStatusUpdateDto
 *
 * Response DTO for Sale Order status updates
 * Used for both updateStatus and updateJobStatus operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaleOrderStatusUpdateDto {
    private Integer id;
    private Integer companyRefId;
    private Integer jStatus;
    private String statusName;
    private String jobStatusName;
    private String cNumberDisplay;
    private LocalDateTime modifiedDate;
    private String message;
}
