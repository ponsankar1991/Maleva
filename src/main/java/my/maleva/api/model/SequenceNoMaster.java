package my.maleva.api.model;

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
 * JPA entity for SequenceNoMaster table
 * Manages sequence numbers for various bill types and document numbering
 */
@Entity
@Table(name = "SequenceNoMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SequenceNoMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "SequenceName", length = 50, nullable = false)
    private String sequenceName;

    @Column(name = "SequenceDate")
    private LocalDateTime sequenceDate;

    @Column(name = "SequenceNo", nullable = false)
    private Integer sequenceNo;

    @Column(name = "SequenceYear")
    private Integer sequenceYear;

    @Column(name = "SequenceMonth")
    private Integer sequenceMonth;
}

