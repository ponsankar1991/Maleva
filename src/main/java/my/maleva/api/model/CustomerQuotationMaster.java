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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity for CustomerQuotationMaster table
 */
@Entity
@Table(name = "CustomerQuotationMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerQuotationMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "CustomerRefId", nullable = false)
    private Integer customerRefId;

    @Column(name = "Date", nullable = false)
    private LocalDateTime date;

    @Column(name = "SymbolRefid")
    private Integer symbolRefid;

    @Column(name = "CNumberDisplay", length = 300, nullable = false)
    private String cNumberDisplay;

    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    @Column(name = "Amount", nullable = false)
    private Float amount;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Remarks", length = 300)
    private String remarks;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", length = 50, nullable = false)
    private String createdBy;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50, nullable = false)
    private String modifiedBy;

    @Column(name = "JobMasterRefId")
    private Integer jobMasterRefId;

    @Column(name = "ExpiryDate")
    private LocalDate expiryDate;
}
