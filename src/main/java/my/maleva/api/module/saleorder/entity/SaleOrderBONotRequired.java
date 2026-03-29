package my.maleva.api.module.saleorder.entity;

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
 * SaleOrderBONotRequired Entity
 * JPA entity for SaleOrderBONotRequired table
 * Tracks Board Officer types that are not required for a sale order
 */
@Entity
@Table(name = "SaleOrderBONotRequired")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderBONotRequired {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SaleOrderMasterRefId")
    private Integer saleOrderMasterRefId;

    @Column(name = "BOTypeId")
    private Integer boTypeId;
}

