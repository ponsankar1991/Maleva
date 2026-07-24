package my.maleva.api.module.employee.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "EmployeeCapability", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"EmployeeId", "CapabilityId"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeCapability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "EmployeeId", nullable = false)
    private Integer employeeId;

    @Column(name = "CapabilityId", nullable = false)
    private Integer capabilityId;

    @Column(name = "GrantedDate")
    private LocalDateTime grantedDate;

    @Column(name = "GrantedBy", length = 50)
    private String grantedBy;

    @Column(name = "IsActive")
    private Boolean isActive;
}
