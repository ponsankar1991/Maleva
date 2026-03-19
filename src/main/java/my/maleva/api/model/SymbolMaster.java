package my.maleva.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SymbolMaster Entity
 * JPA entity for SymbolMaster table
 * Represents currency symbols and their configurations
 * 
 * Performance Optimization: Index on CompanyRefId for lookup operations
 */
@Entity
@Table(name = "SymbolMaster", indexes = {
    @Index(name = "idx_symbol_company", columnList = "CompanyRefId", unique = false)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymbolMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "SName", nullable = false, length = 100)
    private String sName;

    @Column(name = "CName", length = 100)
    private String cName;

    @Column(name = "DFlag", nullable = false)
    private Integer dFlag;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", nullable = false, length = 50)
    private String modifiedBy;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "CurrencyValue")
    private Float currencyValue;

    @Column(name = "QNEID")
    private Integer qneId;
}
