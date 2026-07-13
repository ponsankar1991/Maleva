package my.maleva.api.module.leave.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaveSearchRequestDto {
    @JsonProperty("CompanyRefId")
    private Integer companyRefId;

    @JsonProperty("ApplicantType")
    private Integer applicantType;

    @JsonProperty("ApplicantRefId")
    private Integer applicantRefId;

    @JsonProperty("FromDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fromDate;

    @JsonProperty("ToDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime toDate;
}
