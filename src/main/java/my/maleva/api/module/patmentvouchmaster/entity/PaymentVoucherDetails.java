package my.maleva.api.module.patmentvouchmaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PaymentVoucherDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVoucherDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "PaymentVoucherMasterRefId", nullable = false)
    private Integer paymentVoucherMasterRefId;

    @Column(name = "AccountGroupRefId", nullable = false)
    private Integer accountGroupRefId;

    @Column(name = "Description", length = 300)
    private String description;

    @Column(name = "Amount", nullable = false)
    private Float amount;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "CurrencyValue", nullable = false)
    private Float currencyValue;

    @Column(name = "ActualAmount", nullable = false)
    private Float actualAmount;

    @Column(name = "SubExpenseRefid")
    private Integer subExpenseRefid;

    @Column(name = "PendingPaymentRefId")
    private Integer pendingPaymentRefId;

    @Column(name = "Classification")
    private Integer classification;
}
