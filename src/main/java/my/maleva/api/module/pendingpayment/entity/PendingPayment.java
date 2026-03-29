package my.maleva.api.module.pendingpayment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "PendingPayment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "SubExpenseRefId", nullable = false)
    private Integer subExpenseRefId;

    @Column(name = "DueDate", nullable = false)
    private LocalDate dueDate;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", length = 200, nullable = false)
    private String createdBy;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "PaidStatus")
    private Integer paidStatus;

    @Column(name = "PaidAmount", length = 500)
    private String paidAmount;

    @Column(name = "PaidDate")
    private LocalDateTime paidDate;
}
