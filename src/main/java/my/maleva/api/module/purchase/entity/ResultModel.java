package my.maleva.api.module.purchase.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing the result from stored procedure execution
 * Used for SP_PurchaseMaster and similar stored procedures
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultModel {

    private Integer result;
    private String accountName;
    private Integer id;
    private String msg;
}
