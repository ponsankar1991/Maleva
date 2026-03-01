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
 * SpeedReport Entity
 * JPA entity for SpeedReport table
 * Tracks speed violation reports for trucks
 */
@Entity
@Table(name = "SpeedReport")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "TruckRefId", nullable = false)
    private Integer truckRefId;

    @Column(name = "vehicle", length = 200)
    private String vehicle;

    @Column(name = "time")
    private LocalDateTime time;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "count", length = 100)
    private String count;

    @Column(name = "filled", length = 100)
    private String filled;

    @Column(name = "driver", length = 200)
    private String driver;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;
}

