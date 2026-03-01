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

import java.time.LocalDateTime;

/**
 * VesselPlanningDetails Entity
 * JPA entity for VesselPlanningDetails table
 * Represents detailed vessel planning records
 */
@Entity
@Table(name = "VESSELPLANINGDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VesselPlanningDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "VESSELPLANINGMasterRefId", nullable = false)
    private Integer vesselPlanningMasterRefId;

    @Column(name = "SaleOrderMasterRefId", nullable = false)
    private Integer saleOrderMasterRefId;

    @Column(name = "Remarks", length = 300)
    private String remarks;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;
}

