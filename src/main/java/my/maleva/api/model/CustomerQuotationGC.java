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
 * JPA entity for CustomerQuotationGC table
 */
@Entity
@Table(name = "CustomerQuotationGC")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerQuotationGC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "CustomerMasterRefId", nullable = false)
    private Integer customerMasterRefId;

    @Column(name = "JobMasterRefId", nullable = false)
    private Integer jobMasterRefId;

    @Column(name = "OriginRefId", nullable = false)
    private Integer originRefId;

    @Column(name = "DestinationRefId", nullable = false)
    private Integer destinationRefId;

    @Column(name = "ThreeTon", nullable = false)
    private Float threeTon;

    @Column(name = "FiveTon", nullable = false)
    private Float fiveTon;

    @Column(name = "TenTon", nullable = false)
    private Float tenTon;

    @Column(name = "FourtyFeet", nullable = false)
    private Float fourtyFeet;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "OneTon", nullable = false)
    private Float oneTon;
}
