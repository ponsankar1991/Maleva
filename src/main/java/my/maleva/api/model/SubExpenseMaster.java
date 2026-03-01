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
 * SubExpenseMaster Entity
 * JPA entity for SubExpenseMaster table
 * Represents sub-expense master records with financial and tax details
 */
@Entity
@Table(name = "SubExpenseMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubExpenseMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "ExpenseMasterRefId", nullable = false)
    private Integer expenseMasterRefId;

    @Column(name = "Description", nullable = false, length = 200)
    private String description;

    @Column(name = "DueAmount", nullable = false)
    private Float dueAmount;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "DueFromDate")
    private LocalDateTime dueFromDate;

    @Column(name = "DueToDate")
    private LocalDateTime dueToDate;

    @Column(name = "DueDate")
    private Integer dueDate;

    @Column(name = "AccountRefid", nullable = false)
    private Integer accountRefid;

    @Column(name = "TinNo", length = 100)
    private String tinNo;

    @Column(name = "SSTNo", length = 100)
    private String sstNo;

    @Column(name = "MsicCode", length = 100)
    private String msicCode;

    @Column(name = "ServiceTaxType", length = 100)
    private String serviceTaxType;

    @Column(name = "BankName", length = 100)
    private String bankName;

    @Column(name = "AccountNo", length = 100)
    private String accountNo;

    @Column(name = "GLAccountRefId")
    private Integer glAccountRefId;
}

