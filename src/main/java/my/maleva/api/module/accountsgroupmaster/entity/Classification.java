package my.maleva.api.module.accountsgroupmaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "AccountsGroupClassification")
@Table(name = "Classification")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Classification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Description")
    private String description;

    // int in the database; JDBC converts on read, and nothing writes this
    // read-only table from Java.
    @Column(name = "ClassificationCode")
    private String classificationCode;

    // No Active column exists — the table is just Id, ClassificationCode,
    // Description (verified on LiveMaleva2 and MalevanewDemo). Mapping one
    // made every findAll() fail with "Invalid column name".
}

