package my.maleva.api.module.zbentry.entity;

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

/**
 * Entity for the legacy ZbEntry table.
 * 
 * Note: EntryDate and Amount are stored as varchar in the database schema.
 * They are deliberately kept as String here to exactly map the schema without
 * exceptions. Conversion to LocalDate and BigDecimal occurs in the Mapper layer.
 */
@Entity
@Table(name = "ZbEntry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZbEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "EntryDate", length = 100)
    private String entryDate;

    @Column(name = "ChargeType", length = 200)
    private String chargeType;

    @Column(name = "ZBType", length = 200)
    private String zbType;

    @Column(name = "PortChart", length = 200)
    private String portChart;

    @Column(name = "ZBNumber", length = 200)
    private String zbNumber;

    @Column(name = "VesselName", length = 300)
    private String vesselName;

    @Column(name = "JobNumber", length = 200)
    private String jobNumber;

    @Column(name = "PTWNo", length = 200)
    private String ptwNo;

    @Column(name = "Amount", length = 100)
    private String amount;

    @Column(name = "Active")
    private Integer active;

    @Column(name = "CompanyRefId")
    private Integer companyRefId;
}
