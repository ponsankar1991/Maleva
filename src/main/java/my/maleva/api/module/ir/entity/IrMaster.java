package my.maleva.api.module.ir.entity;

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

import java.time.LocalDateTime;

/**
 * One incident report: something that cost the company money outside normal
 * operations - a spare dropped into the sea, a truck accident, a police
 * compound.
 *
 * <p>{@code FilePath} is deliberately never written by the IR save. The shared
 * {@code /api/attachments} endpoint owns that column (it is passed
 * {@code filePathTable=IRMaster}), so an edit that posts the form again cannot
 * blank out documents that were uploaded after the record was created.
 */
@Entity
@Table(name = "IRMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrMaster {

    /** House rule: 1 is live, 2 is soft-deleted. Reads filter Active &lt;&gt; 2. */
    public static final int ACTIVE = 1;
    public static final int DELETED = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    /** When the incident happened, not when the row was typed. */
    @Column(name = "IRDate", nullable = false)
    private LocalDateTime irDate;

    @Column(name = "IRStatusRefId", nullable = false)
    private Integer irStatusRefId;

    /** What happened. */
    @Column(name = "Description", nullable = false)
    private String description;

    /** Why it happened. */
    @Column(name = "Reason", length = 1000)
    private String reason;

    /** UserRoles.roleId - the department that owns the incident. */
    @Column(name = "DepartmentRefId", nullable = false)
    private Integer departmentRefId;

    /** Snapshot of the enum name, so renaming a role never rewrites history. */
    @Column(name = "DepartmentName", nullable = false, length = 100)
    private String departmentName;

    @Column(name = "VesselName", length = 100)
    private String vesselName;

    @Column(name = "TruckRefId")
    private Integer truckRefId;

    @Column(name = "TruckNo", length = 100)
    private String truckNo;

    @Column(name = "EmployeeRefId")
    private Integer employeeRefId;

    @Column(name = "EmployeeName", length = 100)
    private String employeeName;

    /** Set when it was one of our own drivers; null for an outside driver. */
    @Column(name = "DriverRefId")
    private Integer driverRefId;

    /** Always filled, so the grid shows a name without a join. */
    @Column(name = "DriverName", length = 100)
    private String driverName;

    @Column(name = "ActualAmount")
    private Integer actualAmount;

    /**
     * EmployeeMaster.Id of whoever filed the report, resolved from the logged-in
     * user. Set once on insert and never rewritten, so a later edit by someone
     * else does not change who reported it.
     */
    @Column(name = "CreateEmployeeRefId")
    private Integer createEmployeeRefId;

    /** Comma-joined attachment paths. Written by the attachment endpoint only. */
    @Column(name = "FilePath", length = 3000)
    private String filePath;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Created_By", length = 50)
    private String createdBy;

    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", length = 50)
    private String modifiedBy;
}
