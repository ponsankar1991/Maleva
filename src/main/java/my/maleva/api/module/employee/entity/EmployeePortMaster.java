package my.maleva.api.module.employee.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "EmployeePortMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeePortMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "EmployeeRefId", nullable = false)
    private Integer employeeRefId;

    @Column(name = "PortRefId", nullable = false)
    private Integer portRefId;

    @Column(name = "AssignedBy")
    private Integer assignedBy;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;
}
