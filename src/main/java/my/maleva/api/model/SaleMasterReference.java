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
 * SaleMasterReference Entity
 * JPA entity for SaleMasterReference table
 * Links SaleMaster with SaleOrderMaster records
 */
@Entity
@Table(name = "SaleMasterReference")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleMasterReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SaleMasterRefId", nullable = false)
    private Integer saleMasterRefId;

    @Column(name = "SaleOrderMasterRefId", nullable = false)
    private Integer saleOrderMasterRefId;
}

