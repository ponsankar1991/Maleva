package my.maleva.api.module.master.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PortMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "PortName", nullable = false, length = 50)
    private String portName;

    @Column(name = "Created_Date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "Modified_Date", nullable = false)
    private LocalDateTime modifiedDate;

    @Column(name = "Modified_By", nullable = false, length = 50)
    private String modifiedBy;

    @Column(name = "Active", nullable = false)
    private Integer active;
}

