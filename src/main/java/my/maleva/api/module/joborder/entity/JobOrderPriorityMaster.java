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
@Table(name = "JobOrderPriorityMaster")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobOrderPriorityMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "PriorityName", nullable = false, length = 20)
    private String priorityName;

    @Column(name = "IsActive", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "CreatedDate", nullable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
}
