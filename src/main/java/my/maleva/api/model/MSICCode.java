package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MSICCode")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MSICCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "MSICCode", length = 50)
    private String msicCode;

    @Column(name = "Description", length = 250, nullable = false)
    private String description;
}
