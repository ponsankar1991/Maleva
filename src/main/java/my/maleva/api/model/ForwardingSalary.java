package my.maleva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity for ForwardingSalary table
 */
@Entity
@Table(name = "ForwardingSalary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForwardingSalary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId")
    private Integer companyRefId;

    @Column(name = "RTIMasterRefId", nullable = false)
    private Integer rtiMasterRefId;

    @Column(name = "EmployeeMasterRefId")
    private Integer employeeMasterRefId;

    @Column(name = "EmployeeMasterRefId1")
    private Integer employeeMasterRefId1;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Salary1")
    private Float salary1;

    @Column(name = "Salary2")
    private Float salary2;
}
