package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * RTIDetails Entity
 * Represents Road Transport Infrastructure Detail records
 */
@Entity
@Table(name = "RTIDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RTIDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "RTIMasterRefId", nullable = false)
    private Integer rtiMasterRefId;

    @Column(name = "SaleOrderMasterRefId", nullable = false)
    private Integer saleOrderMasterRefId;

    @Column(name = "Salary")
    private Double salary;

    @Column(name = "PPIC", length = 300)
    private String ppic;

    @Column(name = "DPIC", length = 300)
    private String dpic;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "PWDType")
    private Integer pwdType;

    @Column(name = "PickupDateD")
    private LocalDateTime pickupDateD;

    @Column(name = "DeliveryDateD")
    private LocalDateTime deliveryDateD;

    @Column(name = "OriginD", length = 250)
    private String originD;

    @Column(name = "DestinationD", length = 250)
    private String destinationD;

    @Column(name = "PickupAddressD", length = 2000)
    private String pickupAddressD;

    @Column(name = "DeliveryAddressD", length = 2000)
    private String deliveryAddressD;

    @Column(name = "PickupAddressTimelistD", length = 5000)
    private String pickupAddressTimelistD;

    @Column(name = "PickupAddressQuantityD", length = 500)
    private String pickupAddressQuantityD;

    @Column(name = "DeliveryAddressQuantityD", length = 500)
    private String deliveryAddressQuantityD;

    @Column(name = "DeliveryAddressdatelistD", length = 500)
    private String deliveryAddressdatelistD;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RTIMasterRefId", insertable = false, updatable = false)
    private RTIMaster rtiMaster;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (salary == null) {
            salary = 0.0;
        }
        if (pwdType == null) {
            pwdType = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}

