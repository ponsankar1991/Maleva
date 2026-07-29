package my.maleva.api.module.boardingsettlement.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "BoardingEvent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "SaleOrderMasterRefId")
    private Integer saleOrderMasterRefId;

    @Column(name = "EmployeeRefId")
    private Integer employeeRefId;

    @Column(name = "Amount", length = 200)
    private String amount;

    @Column(name = "TagType", length = 50)
    private String tagType; // e.g., "LOADING", "OFFLOADING", "GENERAL"

    @Column(name = "VesselName", length = 500)
    private String vesselName;

    @Column(name = "BoardingDate")
    private LocalDateTime boardingDate; // Usually ETA or OETA

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;
}
