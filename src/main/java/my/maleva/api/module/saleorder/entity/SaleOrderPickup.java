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
 * SaleOrderPickup Entity
 * JPA entity for SaleOrderPickup table
 * Represents pickup details for a sale order
 */
@Entity
@Table(name = "SaleOrderPickup")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderPickup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SaleOrderMasterRefId", nullable = false)
    private Integer saleOrderMasterRefId;

    @Column(name = "PickupAddress", length = 2000)
    private String pickupAddress;

    @Column(name = "PickupTime")
    private LocalDateTime pickupTime;

    @Column(name = "pickupWeight", length = 100)
    private String pickupWeight;

    @Column(name = "PickupQuantity", length = 100)
    private String pickupQuantity;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;
}

