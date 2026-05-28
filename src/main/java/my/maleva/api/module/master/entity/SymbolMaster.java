package my.maleva.api.module.master.entity;

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
 * SymbolMaster Entity - FIXED VERSION
 * JPA entity for SymbolMaster table
 *
 * IMPORTANT: Field names must match @Mapping source in mapper
 * MapStruct reads Java field names, not @Column names
 */
@Entity
@Table(name = "SymbolMaster", indexes = {
        @Index(name = "idx_symbol_company", columnList = "companyrefid", unique = false)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymbolMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "companyrefid", nullable = false)
    private Integer companyRefId;

    // ✅ FIXED: Changed from 'sName' to 'SName' (uppercase S)
    // MapStruct can now find this field
    @Column(name = "sname", nullable = false, length = 100)
    private String SName;

    // ✅ FIXED: Changed from 'cName' to 'CName' (uppercase C)
    @Column(name = "cname", length = 100)
    private String CName;

    // ✅ FIXED: Changed from 'dFlag' to 'DFlag' (uppercase D)
    @Column(name = "dflag", nullable = false)
    private Integer DFlag;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "modified_by", nullable = false, length = 50)
    private String modifiedBy;

    @Column(name = "active", nullable = false)
    private Integer active;

    @Column(name = "currencyvalue")
    private Float currencyValue;

    @Column(name = "qneid")
    private Integer qneId;
}