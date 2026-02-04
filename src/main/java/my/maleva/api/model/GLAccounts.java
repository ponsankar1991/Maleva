package my.maleva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * JPA entity for GLAccounts table
 */
@Entity
@Table(name = "GLAccounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GLAccounts {

    @Id
    @UuidGenerator
    @Column(name = "Id", columnDefinition = "uniqueidentifier")
    private UUID id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "ParentId", columnDefinition = "uniqueidentifier")
    private UUID parentId;

    @Column(name = "GLAccountCode", length = 20, nullable = false)
    private String glAccountCode;

    @Column(name = "AccountId", columnDefinition = "uniqueidentifier")
    private UUID accountId;

    @Column(name = "SpecialAccountId")
    private Integer specialAccountId;

    @Column(name = "CurrencyId")
    private Integer currencyId;

    @Column(name = "GSTTypeId")
    private Integer gstTypeId;

    @Column(name = "Description", length = 100, nullable = false)
    private String description;

    @Column(name = "DRCR", length = 2)
    private String drCr;

    @Column(name = "IsCreditCard")
    private Boolean isCreditCard;

    @Column(name = "IsActive", nullable = false)
    private Boolean isActive;

    @Column(name = "GSTGroup", length = 1)
    private String gstGroup;

    @Column(name = "IsRevaluation")
    private Boolean isRevaluation;

    @Column(name = "Notes")
    private String notes;

    @Column(name = "IsSubAccount")
    private Boolean isSubAccount;

    @Column(name = "BankAccountNo", length = 100)
    private String bankAccountNo;

    @Column(name = "GSTMSICCode", length = 10)
    private String gstMsicCode;

    @Column(name = "OptimisticLockField")
    private Integer optimisticLockField;

    @Column(name = "SAC", length = 20)
    private String sac;

    @Column(name = "SSTTariffCode", length = 12)
    private String sstTariffCode;

    @Column(name = "RowIndex", nullable = false)
    private Integer rowIndex;

    @Column(name = "HasChildInCoa")
    private Boolean hasChildInCoa;

    @Column(name = "IncludeInCashFlowForecastAdvisor")
    private Boolean includeInCashFlowForecastAdvisor;

    @Column(name = "TariffCodeId")
    private Integer tariffCodeId;

    @Column(name = "ATCCodeId", columnDefinition = "uniqueidentifier")
    private UUID atcCodeId;

    @Column(name = "Description2", length = 100)
    private String description2;

    @Column(name = "Classification")
    private Integer classification;
}
