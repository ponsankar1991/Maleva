package my.maleva.api.module.dashboard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One entry of the dashboard's employee switcher.
 *
 * <p>Maps to the legacy {@code POST /DashBoard/LoadRulesType} response rows, which the
 * jqxComboBox bound with {@code displayMember: "AccountName"} and
 * {@code valueMember: "Id"}. The PascalCase JSON names are kept so a migrated screen can
 * read the same field names the legacy one did.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRuleDto {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("AccountName")
    private String accountName;
}
