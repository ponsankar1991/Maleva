package my.maleva.api.module.master.entity;

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
 * TaxMaster Entity
 * JPA entity for TaxMaster table
 * Represents tax configurations with code, rate, and type
 */
@Entity
@Table(name = "TaxMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "Code", nullable = false, length = 50)
    private String code;

    @Column(name = "Description", nullable = false, length = 50)
    private String description;

    @Column(name = "Tax", nullable = false)
    private Float tax;

    @Column(name = "TaxIO", nullable = false)
    private Integer taxIO;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", nullable = false, length = 50)
    private String modifiedBy;

    @Column(name = "Active", nullable = false)
    private Integer active;
}

