package my.maleva.api.module.agent.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "AgentMaster")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AgentMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "CompanyRefId", nullable = false)
    private Integer companyRefId;

    @Column(name = "AgentName", length = 100, nullable = false)
    private String agentName;

    @Column(name = "LocationCode", length = 30, nullable = false)
    private String locationCode;

    @Column(name = "PhoneNumber", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "AgentRole", length = 20, nullable = false)
    private AgentRole agentRole = AgentRole.BOTH;

    @Column(name = "Active", nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(name = "Created_Date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @CreatedBy
    @Column(name = "Created_By", length = 50, nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "Modified_Date")
    private LocalDateTime modifiedDate;

    @LastModifiedBy
    @Column(name = "Modified_By", length = 50)
    private String modifiedBy;
}
