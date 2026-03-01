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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Summon Entity
 * JPA entity for Summon table
 * Represents traffic summon and violation records for vehicles
 */
@Entity
@Table(name = "Summon")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Summon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "TruckName", nullable = false, length = 100)
    private String truckName;

    @Column(name = "DriverName", nullable = false, length = 100)
    private String driverName;

    @Column(name = "Summon", nullable = false, length = 200)
    private String summon;

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

    @Column(name = "Country", length = 200)
    private String country;

    @Column(name = "PortPass", length = 200)
    private String portPass;

    @Column(name = "TruckLcnMnt", length = 200)
    private String truckLcnMnt;

    @Column(name = "Levy", length = 200)
    private String levy;

    @Column(name = "Fuel", length = 200)
    private String fuel;
}

