package my.maleva.api.module.employee.mapper;

import my.maleva.api.common.constant.UserRoles;
import my.maleva.api.module.employee.dto.EmployeeMasterDto;
import my.maleva.api.module.employee.dto.ProfileDto;
import my.maleva.api.module.employee.entity.EmployeeMaster;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Maps between EmployeeMaster entity / DTOs and the ProfileDto used for
 * the My Profile screen (read + editable fields only).
 */
@Component
public class ProfileMapper {

    /**
     * Convert entity -> ProfileDto (all fields, mutable + immutable).
     */
    public ProfileDto toProfileDto(EmployeeMaster e) {
        if (e == null) return null;
        String roleName = null;
        if (e.getRoleId() != null) {
            roleName = UserRoles.fromId(e.getRoleId()).map(Enum::name).orElse(null);
        }
        return ProfileDto.builder()
                .id(e.getId())
                .cNumberDisplay(e.getCNumberDisplay())
                .roleName(roleName)
                .roleId(e.getRoleId())
                .employeeType(e.getEmployeeType())
                .employeeName(e.getEmployeeName())
                .personId(e.getPersonId())
                .joiningDate(e.getJoiningDate())
                .leavingDate(e.getLeavingDate())
                .email(e.getEmail())
                .mobileNo(e.getMobileNo())
                .emergencyNo(e.getEmergencyNo())
                .address1(e.getAddress1())
                .address2(e.getAddress2())
                .address3(e.getAddress3())
                .city(e.getCity())
                .state(e.getState())
                .zipcode(e.getZipcode())
                .country(e.getCountry())
                .createdDate(e.getCreatedDate())
                .modifiedDate(e.getModifiedDate())
                .modifiedBy(e.getModifiedBy())
                .active(e.getActive())
                .build();
    }

    /**
     * Apply ProfileDto mutable fields back onto an existing entity.
     * Read-only fields (id, cNumberDisplay, role, dates) are intentionally ignored.
     */
    public void updateFromProfileDto(ProfileDto dto, EmployeeMaster entity) {
        if (dto == null || entity == null) return;

        if (dto.getEmployeeName() != null)         entity.setEmployeeName(dto.getEmployeeName());
        if (dto.getPersonId() != null)              entity.setPersonId(dto.getPersonId());
        if (dto.getEmail() != null)                entity.setEmail(dto.getEmail());
        if (dto.getMobileNo() != null)             entity.setMobileNo(dto.getMobileNo());
        if (dto.getEmergencyNo() != null)          entity.setEmergencyNo(dto.getEmergencyNo());
        if (dto.getAddress1() != null)             entity.setAddress1(dto.getAddress1());
        if (dto.getAddress2() != null)             entity.setAddress2(dto.getAddress2());
        if (dto.getAddress3() != null)             entity.setAddress3(dto.getAddress3());
        if (dto.getCity() != null)                 entity.setCity(dto.getCity());
        if (dto.getState() != null)                entity.setState(dto.getState());
        if (dto.getZipcode() != null)              entity.setZipcode(dto.getZipcode());
        if (dto.getCountry() != null)              entity.setCountry(dto.getCountry());
    }
}
