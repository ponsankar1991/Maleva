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

import java.time.LocalDateTime;

/**
 * SaleOrderBO Entity
 * JPA entity for SaleOrderBO table
 * Represents Board Officer requirements for a sale order
 */
@Entity
@Table(name = "SaleOrderBO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SaleOrderMasterRefId", nullable = false)
    private Integer saleOrderMasterRefId;

    @Column(name = "BOTypeId", nullable = false)
    private Integer boTypeId;

    @Column(name = "Status", nullable = false)
    private Integer status;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;
}

