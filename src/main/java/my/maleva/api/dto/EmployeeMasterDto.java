package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import my.maleva.api.util.UserRoles;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotBlank
    @Size(max = 500)
    private String employeeName;

    @NotBlank
    @Size(max = 100)
    private String employeeType;

    @NotBlank
    @Size(max = 300)
    private String cNumberDisplay;

    @NotNull
    private Integer cNumber;

    @Size(max = 300)
    private String address1;

    @Size(max = 300)
    private String address2;

    @Size(max = 100)
    private String city;

    @Size(max = 50)
    private String zipcode;

    @Size(max = 50)
    private String country;

    @Size(max = 100)
    private String gstNo;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 50)
    private String mobileNo;

    @Size(max = 50)
    private String userName;

    @Size(max = 50)
    private String password;

    @Size(max = 50)
    private String latitude;

    @Size(max = 50)
    private String longitude;

    @Size(max = 500)
    private String tokenId;

    @NotNull
    private Integer active;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    @Size(max = 100)
    private String state;

    @Size(max = 300)
    private String address3;

    @Size(max = 100)
    private String personId;

    @NotNull
    private Integer accountRefid;

    // role id column from DB (numeric(4))
    @NotNull
    @JsonProperty("roleId")
    private Integer roleId;

    // optional enum representation for convenience in DTOs / API
    @JsonProperty("role")
    private UserRoles role;

    // expose role name separately (string) for clients that prefer text
    @JsonProperty("roleName")
    public String getRoleName() {
        return role == null ? null : role.name();
    }

    @Size(max = 100)
    private String tinNo;

    @Size(max = 100)
    private String sstNo;

    @Size(max = 100)
    private String msicCode;

    @Size(max = 100)
    private String serviceTaxType;

    @Size(max = 100)
    private String bankName;

    @Size(max = 100)
    private String accountNo;

    @Size(max = 50)
    private String rulesType;

    @Size(max = 50)
    private String qneCode;

    @Size(max = 50)
    private String qneId;

    private LocalDate joiningDate;
    private LocalDate leavingDate;

    @Size(max = 50)
    private String emergencyNo;

    @Size(max = 100)
    private String appPassword;

    @Size(max = 255)
    private String employeecurrency;
}
