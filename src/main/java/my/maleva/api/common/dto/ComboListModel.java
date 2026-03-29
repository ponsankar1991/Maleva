package my.maleva.api.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ComboListModel - DTO for combo box/dropdown list data
 * Maps from database queries for UI dropdowns
 * Equivalent to .NET ComboListModel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComboListModel {

    @JsonProperty("Id")
    private Integer id;

    @JsonProperty("AccountName")
    private String accountName;

    @JsonProperty("AccountName1")
    private String accountName1;

    @JsonProperty("AccountCode")
    private String accountCode;

    @JsonProperty("EmployeeType")
    private String employeeType;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("OEmail1")
    private String oEmail1;

    /**
     * Constructor for simple Id + AccountName mapping
     * Used for truck combo queries
     */
    public ComboListModel(Integer id, String accountName) {
        this.id = id;
        this.accountName = accountName;
    }
}

