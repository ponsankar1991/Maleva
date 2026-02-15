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

/**
 * Minimal JPA entity for the SymbolMaster table (used in customer queries).
 * Only the columns required by `CustomerQueryRepository` are modelled here.
 */
@Entity
@Table(name = "SymbolMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymbolMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SName", length = 500)
    private String sName;

    @Column(name = "CompanyRefId")
    private Integer companyRefId;
}
