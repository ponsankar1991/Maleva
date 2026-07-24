package my.maleva.api.module.employee.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Capability")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Capability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "Code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(name = "DisplayName", length = 100, nullable = false)
    private String displayName;

    @Column(name = "IsActive")
    private Boolean isActive;
}
