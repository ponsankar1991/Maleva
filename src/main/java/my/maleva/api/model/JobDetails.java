package my.maleva.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "JobDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "JobMasterRefId", nullable = false)
    private Integer jobMasterRefId;

    @Column(name = "Description", length = 100, nullable = false)
    private String description;

    @Column(name = "Active", nullable = false)
    private Integer active;

    @Column(name = "Mandatory", nullable = false)
    private Integer mandatory;

    @Column(name = "Status", nullable = false)
    private Integer status;
}
