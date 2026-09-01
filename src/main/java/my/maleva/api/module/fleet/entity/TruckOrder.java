package my.maleva.api.module.fleet.entity;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One truck booked for one day - the document behind the Truck Order Calendar.
 *
 * <p>Maps the legacy {@code TruckOrderMaster} table. Two of its columns are
 * deliberately absent: {@code saleordermasterrefid} and {@code jobcategory}
 * exist on the table but neither the legacy screen nor SP_TruckOrderMaster ever
 * wrote them, so mapping them here would let an edit blank a value this screen
 * has no business touching.
 *
 * <p>{@code OrderDate} is a SQL {@code date}, not a datetime, so a booking has
 * no time of day and the "already booked" rule is a whole-day rule.
 */
@Entity
@Table(name = "TruckOrderMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruckOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "TruckRefId", nullable = false)
    private Integer truckRefId;

    /** Null when nobody is recorded: the procedure stored 0 as NULL, and so do we. */
    @Column(name = "EmployeeRefId")
    private Integer employeeRefId;

    /** Running number within the company. Fixed at insert and never re-pointed on edit. */
    @Column(name = "CNumber", nullable = false)
    private Integer cNumber;

    /** The printed order number, {@code ORD} + 9 digits. Also fixed at insert. */
    @Column(name = "CNumberDisplay", nullable = false, length = 50)
    private String cNumberDisplay;

    @Column(name = "OrderDate", nullable = false)
    private LocalDate orderDate;

    @Column(name = "Status", nullable = false, length = 30)
    private String status;

    @Column(name = "Remarks", length = 300)
    private String remarks;

    /** 1 live, 2 deleted. The legacy screen could only ever write 1. */
    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", nullable = false, length = 50)
    private String modifiedBy;
}
