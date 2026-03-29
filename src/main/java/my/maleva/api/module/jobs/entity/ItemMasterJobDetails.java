package my.maleva.api.module.jobs.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ItemMasterJobDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemMasterJobDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "ItemMasterRefId", nullable = false)
    private Integer itemMasterRefId;

    @Column(name = "JobMasterRefId", nullable = false)
    private Integer jobMasterRefId;

    @Column(name = "Active", nullable = false)
    private Integer active;
}
