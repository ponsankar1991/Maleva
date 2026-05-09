package my.maleva.api.module.accounting.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for COA Expense Response
 *
 * Equivalent to C# AccountsGroupMasterModel used in SelectCOAExpense
 * Maps from GLAccounts entity and Classification lookup
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class COAExpenseResponseDto {

    /**
     * RowIndex from GLAccounts (unique identifier for GL Account)
     */
    private Integer id;

    /**
     * Company Reference ID
     */
    private Integer comid;

    /**
     * Classification from GLAccounts
     */
    private Integer classification;

    /**
     * GL Account Code (GLAccountCode)
     */
    private String accountCode;

    /**
     * Classification Name (from Classification table)
     * Will be populated if classification join is available
     */
    private String classificationName;

    /**
     * Account Name (Description from GLAccounts)
     */
    private String accountName;

    /**
     * Account Name 1 (Description2 from GLAccounts) - alternate description
     */
    private String accountName1;

    /**
     * Parent Account Name
     */
    private String parentName;

    /**
     * Parent ID (ParentId from GLAccounts)
     */
    private String parentId;

    /**
     * Root ID
     */
    private String rootId;

    /**
     * Active status (IsActive from GLAccounts)
     */
    private Boolean active;

    /**
     * QNE Code
     */
    private String qneCode;

    /**
     * Update ID
     */
    private String updateId;
}

