package my.maleva.api.module.pettycash.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PettyCashDetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Items", length = 100)
    private String items;

    @Column(name = "Amount", precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "Notes", length = 255)
    private String notes;

    /**
     * The chart-of-accounts account this line is booked to — a
     * {@code GLAccounts.RowIndex}, exactly as
     * {@code PaymentVoucherDetails.AccountGroupRefId} holds it.
     *
     * <p>Nullable and optional: existing rows predate the column, and an unset
     * dropdown is stored as NULL rather than 0.
     */
    @Column(name = "AccountGroupRefId")
    private Integer accountGroupRefId;

    /**
     * The e-Invoice classification for this line — a {@code Classification.Id},
     * exactly as {@code PaymentVoucherDetails.Classification} holds it.
     *
     * <p>Nullable and optional: existing rows predate the column, and an unset
     * picker is stored as NULL rather than 0.
     */
    @Column(name = "Classification")
    private Integer classification;

    @Column(name = "PettyCashMasterRefId", nullable = false)
    private Integer pettyCashMasterRefId;

    @Column(name = "Active")
    private Integer active;

    @Column(name = "Created_Date")
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;
}
