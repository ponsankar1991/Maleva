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
 * JPA entity for EngineHours table
 */
@Entity
@Table(name = "EngineHours")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngineHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "TruckRefId", nullable = false)
    private Integer truckRefId;

    @Column(name = "beginTime", nullable = false)
    private LocalDateTime beginTime;

    @Column(name = "endTime", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "beginLocation", length = 800)
    private String beginLocation;

    @Column(name = "endLocation", length = 800)
    private String endLocation;

    @Column(name = "totalTime", length = 100)
    private String totalTime;

    @Column(name = "inMotion", length = 100)
    private String inMotion;

    @Column(name = "idling", length = 100)
    private String idling;

    @Column(name = "mileage", length = 100)
    private String mileage;

    @Column(name = "consumedbyFLSinidlerun", length = 100)
    private String consumedbyFLSinidlerun;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;
}
