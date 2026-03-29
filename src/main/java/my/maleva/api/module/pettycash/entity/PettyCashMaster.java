package my.maleva.api.module.pettycash.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PettyCashMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PettyCashMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CNumberDisplay", length = 50)
    private String cNumberDisplay;

    @Column(name = "EmployeeRefId", nullable = false)
    private Integer employeeRefId;

    @Column(name = "PaymentStatus", length = 20)
    private String paymentStatus;

    @Column(name = "PettyCashDate")
    private LocalDateTime pettyCashDate;

    @Column(name = "Remark", length = 255)
    private String remark;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "Active")
    private Integer active;

    @Column(name = "Amount", length = 100)
    private String amount;

    @Column(name = "Created_Date")
    private LocalDateTime createdDate;

    @Column(name = "Created_By", length = 50)
    private String createdBy;

    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50)
    private String modifiedBy;

    @Column(name = "CompanyRefId")
    private Integer companyRefId;

    @Column(name = "CNumber")
    private Integer cNumber;

    @Column(name = "Department", length = 255)
    private String department;
}
