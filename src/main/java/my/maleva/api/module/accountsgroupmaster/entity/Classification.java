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

    @Column(name = "ClassificationCode")
    private String classificationCode;

    @Column(name = "Active")
    private Integer active;
}

