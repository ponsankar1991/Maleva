package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JobTypeMaster Entity
 * Performance Optimization: Index on CompanyRefId for lookup operations
 */
@Entity
@Table(name = "JobTypeMaster", indexes = {
    @Index(name = "idx_job_type_company", columnList = "CompanyRefId", unique = false)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobTypeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "Name", length = 100, nullable = false)
    private String name;

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
}
