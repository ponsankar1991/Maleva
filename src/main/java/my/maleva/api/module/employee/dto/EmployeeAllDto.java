 package my.maleva.api.module.employee.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for SelectEmployeeAll endpoint that includes employee details with AccountCode
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAllDto {
    private Integer id;
    private Integer companyRefId;
    private String employeeName;
    private String employeeType;
    private String cNumberDisplay;
    private Integer cNumber;
    private String address1;
    private String address2;
    private String city;
    private String zipcode;
    private String country;
    private String gstNo;
    private String email;
    private String mobileNo;
    private String userName;
    private String password;
    private String latitude;
    private String longitude;
    private String tokenId;
    private Integer active;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    private String state;
    private String address3;
    private String personId;
    private Integer accountRefId;
    private String appPassword;
    private String tinNo;
    private String sstNo;
    private String msicCode;
    private String serviceTaxType;
    private String bankName;
    private String accountNo;
    private String rulesType;
    private String qneCode;
    private String qneId;
    private java.time.LocalDate joiningDate;
    private java.time.LocalDate leavingDate;
    private String emergencyNo;
    private String employeecurrency;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Integer roleId;
    
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private Integer permisionId;

    private java.util.List<Integer> capabilityIds;

    // Additional field from AccountsGroupMaster
    private String accountCode;

    // Automatically resolve the textual role name based on the roleId
    @com.fasterxml.jackson.annotation.JsonProperty("roleName")
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS)
    public String getRoleName() {
        if (this.roleId != null) {
            return my.maleva.api.common.constant.UserRoles.fromId(this.roleId)
                    .map(Enum::name)
                    .orElse(null);
        }
        return null;
    }
}

