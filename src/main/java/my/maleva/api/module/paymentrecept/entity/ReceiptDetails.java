package my.maleva.api.module.paymentrecept.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * ReceiptDetails Entity
 * Represents detail records for receipts
 */
@Entity
@Table(name = "ReceiptDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId")
    private Integer companyRefId;

    @Column(name = "ReceiptRefId", nullable = false)
    private Integer receiptRefId;

    @Column(name = "SaleMasterRefId")
    private Integer saleMasterRefId;

    @Column(name = "CustomeropenRefId")
    private Integer customerOpenRefId;

    @Column(name = "ReceiptAmount", nullable = false, precision = 18, scale = 2)
    private BigDecimal receiptAmount;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "CurrencyValue")
    private Double currencyValue;

    @Column(name = "ActualAmount")
    private Double actualAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ReceiptRefId", insertable = false, updatable = false)
    private Receipt receipt;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}

