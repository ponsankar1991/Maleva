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

import java.math.BigDecimal;
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

    /** Null only for an {@link TruckBookingType#OUTSIDE} order, which uses no truck of ours. */
    @Column(name = "TruckRefId")
    private Integer truckRefId;

    /** A {@link TruckBookingType} name. Rows older than the column read OWN. */
    @Column(name = "BookingType", nullable = false, length = 10)
    private String bookingType;

    /** The hired truck's plate or name. Set for OUTSIDE orders only. */
    @Column(name = "OutsideTruckName", length = 100)
    private String outsideTruckName;

    /** Who the outside truck was hired from. Optional, OUTSIDE orders only. */
    @Column(name = "OutsideSupplierName", length = 200)
    private String outsideSupplierName;

    /** The {@link TruckSizeClass} code the dispatcher searched for. Reporting only. */
    @Column(name = "TruckSizeClass", length = 10)
    private String truckSizeClass;

    /** Who the order is for - {@code Customer.Id}. Null when nobody was named. */
    @Column(name = "CustomerRefId")
    private Integer customerRefId;

    /**
     * How much cargo, counted in {@link #quantityUnit}. Null when not recorded.
     *
     * <p>A number, not the free text the sale order keeps ({@code "8 PLT + 1
     * PIPE"}), so a day's load can actually be added up.
     */
    @Column(name = "Quantity", precision = 18, scale = 2)
    private BigDecimal quantity;

    /** A {@link TruckLoadUnit} code, e.g. {@code PLT}. Set together with the quantity. */
    @Column(name = "QuantityUnit", length = 20)
    private String quantityUnit;

    /**
     * Where the load is picked up, e.g. {@code SINGAPORE}. Stored trimmed and in
     * capitals so the same place always reads the same way - the return-load
     * search compares these.
     */
    @Column(name = "Origin", length = 200)
    private String origin;

    /** Where the load goes, e.g. {@code SEREMBAN}. Same form as {@link #origin}. */
    @Column(name = "Destination", length = 200)
    private String destination;

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
