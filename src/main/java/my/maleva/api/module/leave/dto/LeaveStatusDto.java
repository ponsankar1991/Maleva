package my.maleva.api.module.leave.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LeaveStatusDto {
    @JsonProperty("Id")
    private Integer id;
    
    @JsonProperty("StatusName")
    private String statusName;
    
    @JsonProperty("DisplayName")
    private String displayName;
    
    @JsonProperty("Active")
    private Boolean active;
}
