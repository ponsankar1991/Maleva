package my.maleva.api.module.planning.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PLANINGDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanningDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "PLANINGMasterRefId", nullable = false)
    private Integer planningMasterRefId;

    @Column(name = "SaleOrderMasterRefId", nullable = false)
    private Integer saleOrderMasterRefId;

    @Column(name = "TruckRefid")
    private Integer truckRefId;

    @Column(name = "Remarks", length = 300)
    private String remarks;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "OriginD", length = 150)
    private String originD;

    @Column(name = "DestinationD", length = 150)
    private String destinationD;

    @Column(name = "PickupDateD")
    private LocalDateTime pickupDateD;

    @Column(name = "DeliveryDateD")
    private LocalDateTime deliveryDateD;

    @Column(name = "SortBy", nullable = false)
    private Integer sortBy;

    @Column(name = "TruckNameD", length = 200)
    private String truckNameD;

    @Column(name = "DriverNameD", length = 200)
    private String driverNameD;

    @Column(name = "pickuptimelist", length = 500)
    private String pickupTimeList;

    @Column(name = "pickupQuantitylist", length = 500)
    private String pickupQuantityList;

    @Column(name = "DeliveryQuantitylist", length = 500)
    private String deliveryQuantityList;

    @Column(name = "Delivertimelist", length = 500)
    private String deliveryTimeList;


    @Column(name = "DriverName", length = 200)
    private String driverName;


    @Column(name = "DriverRefId")
    private Integer driverRefId;
}

