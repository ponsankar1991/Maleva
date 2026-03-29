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
 * SubcdiyEntry Entity
 * JPA entity for SubcdiyEntry table
 * Represents subsidy entry records with amount tracking
 */
@Entity
@Table(name = "SubcdiyEntry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubcdiyEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "ActualAmount", length = 200)
    private String actualAmount;

    @Column(name = "Amount")
    private BigDecimal amount;

    @Column(name = "EntryDate")
    private LocalDate entryDate;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "ModifiedDate", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Active")
    private Integer active;
}

