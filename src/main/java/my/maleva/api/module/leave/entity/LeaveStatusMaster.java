package my.maleva.api.module.leave.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "LeaveStatusMaster")
public class LeaveStatusMaster {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "StatusName", length = 20)
    private String statusName;
    
    @Column(name = "DisplayName", length = 50)
    private String displayName;
    
    @Column(name = "Active")
    private Boolean active;
}
