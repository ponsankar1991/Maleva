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

import java.time.LocalDateTime;

/**
 * TollEntryDetails Entity
 * JPA entity for TollEntryDetails table
 * Represents detailed toll transaction records
 */
@Entity
@Table(name = "TollEntryDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TollEntryDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "TollEntryMasterRefId", nullable = false)
    private Integer tollEntryMasterRefId;

    @Column(name = "EntryAmount", nullable = false)
    private Float entryAmount;

    @Column(name = "EntryDate", nullable = false)
    private LocalDateTime entryDate;

    @Column(name = "EntryTime", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "TransType", nullable = false, length = 200)
    private String transType;

    @Column(name = "EntryBalance", nullable = false)
    private Float entryBalance;

    @Column(name = "VehicleClass")
    private Integer vehicleClass;

    @Column(name = "ExitSP", length = 200)
    private String exitSP;

    @Column(name = "ExitLocation", length = 200)
    private String exitLocation;

    @Column(name = "EntrySP", length = 200)
    private String entrySP;

    @Column(name = "EntryLocation", length = 200)
    private String entryLocation;

    @Column(name = "TransNo", length = 200)
    private String transNo;

    @Column(name = "TransactionID", length = 200)
    private String transactionID;

    @Column(name = "VehicleNumber", length = 200)
    private String vehicleNumber;

    @Column(name = "MFGNumber", length = 200)
    private String mfgNumber;
}

