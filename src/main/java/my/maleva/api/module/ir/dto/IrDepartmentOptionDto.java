package my.maleva.api.module.ir.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row of the department dropdown, built from the UserRoles enum.
 *
 * <p>Served from this module rather than reused from {@code /api/employees/types}
 * so the IR screen keeps the raw enum name it also stores as
 * {@code IRMaster.DepartmentName}. That endpoint rewrites two of the names for
 * legacy parity (CUSTOMERSERVICE becomes "CustomerServiceAdmin"), which would
 * make the dropdown label and the stored snapshot disagree.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrDepartmentOptionDto {

    /** UserRoles.roleId. */
    private Integer id;

    /** UserRoles enum name, exactly as it is stored. */
    private String name;
}
