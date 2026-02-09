package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PaymentDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId")
    private Integer companyRefId;

    @Column(name = "PaymentRefId", nullable = false)
    private Integer paymentRefId;

    @Column(name = "PurchaseMasterRefId")
    private Integer purchaseMasterRefId;

    @Column(name = "SupplieropenRefId")
    private Integer supplieropenRefId;

    @Column(name = "PaymentAmount", nullable = false, precision = 19, scale = 4)
    private BigDecimal paymentAmount;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "BillMasterRefId")
    private Integer billMasterRefId;

    @Column(name = "CurrencyValue", nullable = false)
    private Float currencyValue;

    @Column(name = "ActualAmount", nullable = false)
    private Float actualAmount;
}
