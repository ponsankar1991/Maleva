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
import java.time.LocalDateTime;

/**
 * JPA entity for CardMaster table
 */
@Entity
@Table(name = "CardMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CardType", length = 50, nullable = false)
    private String cardType;

    @Column(name = "Scharge", nullable = false, precision = 18, scale = 2)
    private BigDecimal scharge;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", length = 200, nullable = false)
    private String createdBy;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 200, nullable = false)
    private String modifiedBy;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;
}
