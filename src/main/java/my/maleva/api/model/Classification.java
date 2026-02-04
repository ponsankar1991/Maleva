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

/**
 * JPA entity for Classification table
 */
@Entity
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

    @Column(name = "ClassificationCode")
    private Integer classificationCode;

    @Column(name = "Description", length = 250)
    private String description;
}
