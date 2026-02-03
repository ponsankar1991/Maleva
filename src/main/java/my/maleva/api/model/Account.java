package my.maleva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

/**
 * JPA entity for Accounts table
 */
@Entity
@Table(name = "Accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "Id", nullable = false, updatable = false, columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "AccountCode", length = 20, nullable = false)
    private String accountCode;

    @Column(name = "Description", length = 100)
    private String description;

    @Column(name = "ChartSequence")
    private Integer chartSequence;

    @Column(name = "DRCR", length = 2)
    private String drcr;

    @Column(name = "OptimisticLockField")
    private Integer optimisticLockField;

    @Column(name = "ChartSequencePH")
    private Integer chartSequencePh;

    @Column(name = "AccountCodePH", length = 100)
    private String accountCodePh;

    @Column(name = "RowIndex", nullable = false)
    private Integer rowIndex;

    // audit fields
    @Column(name = "Created_Date")
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50)
    private String modifiedBy;

    @Column(name = "Active")
    private Integer active;
}
