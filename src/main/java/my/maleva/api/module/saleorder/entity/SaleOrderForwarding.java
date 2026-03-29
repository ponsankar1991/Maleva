package my.maleva.api.module.saleorder.entity;

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
 * SaleOrderForwarding Entity
 * JPA entity for SaleOrderForwarding table
 * Represents forwarding details for a sale order
 */
@Entity
@Table(name = "SaleOrderForwarding")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleOrderForwarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SaleOrderMasterRefId", nullable = false)
    private Integer saleOrderMasterRefId;

    @Column(name = "ForwardingDate")
    private LocalDateTime forwardingDate;

    @Column(name = "ForwardingName", length = 200)
    private String forwardingName;

    @Column(name = "EnterRef", length = 200)
    private String enterRef;

    @Column(name = "SMKNo", length = 200)
    private String smkNo;

    @Column(name = "SealByRefId")
    private Integer sealByRefId;

    @Column(name = "SealAmount")
    private BigDecimal sealAmount;

    @Column(name = "BreakSealByRefId")
    private Integer breakSealByRefId;

    @Column(name = "BreakSealAmount")
    private BigDecimal breakSealAmount;

    @Column(name = "ExitRef", length = 200)
    private String exitRef;

    @Column(name = "Quantity")
    private BigDecimal quantity;

    @Column(name = "S1", length = 200)
    private String s1;

    @Column(name = "S2", length = 200)
    private String s2;

    @Column(name = "RowNumber", nullable = false)
    private Integer rowNumber;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @Column(name = "ModifiedDate")
    private LocalDateTime modifiedDate;
}

