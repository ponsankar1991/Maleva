package my.maleva.api.module.fleet.entity;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TruckSpareParts Entity
 * JPA entity for TruckSpareParts table
 * Represents spare parts records for trucks
 */
@Entity
@Table(name = "TruckSpareParts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckSpareParts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "TruckName", nullable = false, length = 100)
    private String truckName;

    @Column(name = "DriverName", nullable = false, length = 100)
    private String driverName;

    @Column(name = "SpareParts", nullable = false, length = 200)
    private String spareParts;

    @Column(name = "Amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "DocumentPath", length = 200)
    private String documentPath;

    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 100)
    private String modifiedBy;

    @Column(name = "Comid")
    private Integer comid;

    @Column(name = "EntryDate")
    private LocalDate entryDate;
}

