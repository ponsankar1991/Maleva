package my.maleva.api.agentcompany.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA Entity for AgentCompanyMaster table.
 * Represents an agent company in the system.
 *
 * Fields:
 * - Id: Primary key (auto-generated identity)
 * - CompanyRefId: Foreign key reference to Company
 * - Name: Agent company name (max 100 characters)
 * - DFlag: Deletion flag (default 0)
 * - CreatedDate: Record creation timestamp
 * - ModifiedDate: Record modification timestamp
 * - ModifiedBy: User who last modified the record
 * - Active: Status flag (1=active, 2=deleted/inactive)
 */
@Entity
@Table(name = "AgentCompanyMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentCompanyMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "Name", length = 100, nullable = false)
    private String name;

    @Column(name = "DFlag", nullable = false)
    private Integer dFlag;

    @Column(name = "Created_Date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "Active", nullable = false)
    private Integer active;
}
