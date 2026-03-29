package my.maleva.api.module.jobs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "JobStatusDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatusDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "JobMasterRefId", nullable = false)
    private Integer jobMasterRefId;

    @Column(name = "Status", nullable = false)
    private Integer status;

    @Column(name = "MinStatus", nullable = false)
    private Integer minStatus;

    @Column(name = "Sort", nullable = false)
    private Integer sort;

    @Column(name = "MasterStatus")
    private Integer masterStatus;
}
