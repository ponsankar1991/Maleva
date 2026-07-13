package my.maleva.api.module.leave.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LeaveTypeDto {
    @JsonProperty("Id")
    private Integer id;
    
    @JsonProperty("LeaveTypeName")
    private String leaveTypeName;
    
    @JsonProperty("Active")
    private Boolean active;
}
