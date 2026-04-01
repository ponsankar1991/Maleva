package my.maleva.api.module.jobs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JobStatusMaster Entity
 * Performance Optimization: Index on CompanyRefId for lookup operations
 */
@Entity
@Table(name = "JobStatusMaster", indexes = {
    @Index(name = "idx_job_status_company", columnList = "CompanyRefId", unique = false)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStatusMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "Name", length = 100, nullable = false)
    private String name;

    @Column(name = "Svalue", nullable = false)
    private Integer svalue;

    @Column(name = "DFlag", nullable = false)
    private Integer dFlag;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "MId")
    private Integer mId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MId", referencedColumnName = "Id", insertable = false, updatable = false)
    private JobStatusMaster parentStatus;
}
