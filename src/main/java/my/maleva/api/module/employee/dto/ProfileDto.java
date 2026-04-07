package my.maleva.api.module.employee.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for the authenticated employee's own profile view/edit.
 * Exposes the fields that a logged-in user should be able to read and modify about themselves.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDto {

    // --- Read-only identity fields ---
    private Integer id;
    private String cNumberDisplay;      // employee code, e.g. "E000000001"
    private String roleName;            // human-readable role
    private Integer roleId;
    private String employeeType;        // e.g. "ADMIN", "DRIVER"

    // --- Editable personal ---
    private String employeeName;
    private String personId;            // NRIC / IC number
    private LocalDate joiningDate;
    private LocalDate leavingDate;

    // --- Editable contact ---
    private String email;
    private String mobileNo;
    private String emergencyNo;
    private String address1;
    private String address2;
    private String address3;
    private String city;
    private String state;
    private String zipcode;
    private String country;

    // --- Read-only meta ---
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    private Integer active;
}
