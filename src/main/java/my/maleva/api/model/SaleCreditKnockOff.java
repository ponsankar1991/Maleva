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
 * SaleCreditKnockOff Entity
 * JPA entity for SaleCreditKnockOff table
 */
@Entity
@Table(name = "SaleCreditKnockOff")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleCreditKnockOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId")
    private Integer companyRefId;

    @Column(name = "SaleCreditMasterRefId", nullable = false)
    private Integer saleCreditMasterRefId;

    @Column(name = "SaleMasterRefId")
    private Integer saleMasterRefId;

    @Column(name = "CustomeropenRefId")
    private Integer customerOpenRefId;

    @Column(name = "SaleCreditAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal saleCreditAmount;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "CurrencyValue")
    private Double currencyValue;

    @Column(name = "ActualAmount")
    private Double actualAmount;
}

