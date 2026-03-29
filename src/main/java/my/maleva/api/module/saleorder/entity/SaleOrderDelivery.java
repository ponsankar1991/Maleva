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
 * SaleOrderDelivery Entity
 * JPA entity for SaleOrderDelivery table
 * Represents delivery details for a sale order
 */
@Entity
@Table(name = "SaleOrderDelivery")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SaleOrderMasterRefId", nullable = false)
    private Integer saleOrderMasterRefId;

    @Column(name = "DeliveryAddress", nullable = false, length = 2000)
    private String deliveryAddress;

    @Column(name = "DeliveryTime")
    private LocalDateTime deliveryTime;

    @Column(name = "DeliveryWeight", length = 100)
    private String deliveryWeight;

    @Column(name = "DeliveryQuantity", length = 100)
    private String deliveryQuantity;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;
}

