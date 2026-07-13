package my.maleva.api.module.leave.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "LeaveTypeMaster")
public class LeaveTypeMaster {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "LeaveTypeName", length = 50)
    private String leaveTypeName;
    
    @Column(name = "Active")
    private Boolean active;
}
