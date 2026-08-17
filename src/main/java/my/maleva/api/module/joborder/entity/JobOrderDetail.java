package my.maleva.api.module.joborder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "JobOrderDetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "JobOrderMasterRefId", nullable = false)
    private Integer jobOrderMasterRefId;

    @Column(name = "ProblemName", nullable = false, length = 200)
    private String problemName;

    @Column(name = "ProductUse", length = 200)
    private String productUse;

    @Column(name = "ProductRefId")
    private Integer productRefId;

    @Column(name = "Cost", precision = 12, scale = 2)
    private BigDecimal cost;

    @Column(name = "Remarks", length = 500)
    private String remarks;

    @Column(name = "Active", nullable = false)
    private Integer active = 1;

    @Column(name = "CreatedBy")
    private Integer createdBy;

    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "ModifiedBy")
    private Integer modifiedBy;

    @Column(name = "ModifiedDate")
    private LocalDateTime modifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "JobOrderMasterRefId", insertable = false, updatable = false)
    private JobOrderMaster jobOrderMaster;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (active == null) {
            active = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
