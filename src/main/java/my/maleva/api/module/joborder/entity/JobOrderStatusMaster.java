package my.maleva.api.module.joborder.entity;

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

@Entity
@Table(name = "JobOrderStatusMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOrderStatusMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "StatusName", nullable = false, length = 30)
    private String statusName;

    @Column(name = "IsActive", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "CreatedDate", nullable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
}
