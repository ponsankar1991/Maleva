package my.maleva.api.module.accounting.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import my.maleva.api.common.entity.BaseAuditEntity;

/**
 * JPA entity for Accounts table
 */
@Entity
@Table(name = "Accounts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Account extends BaseAuditEntity {

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

    @Column(name = "Active")
    private Integer active;
}
