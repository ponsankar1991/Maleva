package my.maleva.api.module.leave.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LeaveRequestResponseDto {
    @JsonProperty("Id")
    private Integer id;
    
    @JsonProperty("CompanyRefId")
    private Integer companyRefId;
    
    @JsonProperty("ApplicantType")
    private Integer applicantType;
    
    @JsonProperty("ApplicantRefId")
    private Integer applicantRefId;

    @JsonProperty("ApplicantName")
    private String applicantName;

    @JsonProperty("ReviewedByName")
    private String reviewedByName;
    
    @JsonProperty("LeaveTypeRefId")
    private Integer leaveTypeRefId;
    
    @JsonProperty("FromDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fromDate;
    
    @JsonProperty("ToDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime toDate;
    
    @JsonProperty("TotalDays")
    private Integer totalDays;
    
    @JsonProperty("Reason")
    private String reason;
    
    @JsonProperty("StatusRefId")
    private Integer statusRefId;
    
    @JsonProperty("CreatedBy")
    private Integer createdBy;
    
    @JsonProperty("ReviewedBy")
    private Integer reviewedBy;
    
    @JsonProperty("ReviewRemark")
    private String reviewRemark;
    
    @JsonProperty("Active")
    private Integer active;
    
    @JsonProperty("LeaveTypeName")
    private String leaveTypeName;
    
    @JsonProperty("StatusName")
    private String statusName;
}
