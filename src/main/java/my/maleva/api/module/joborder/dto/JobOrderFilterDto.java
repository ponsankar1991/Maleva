package my.maleva.api.module.joborder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOrderFilterDto {
    private Integer companyRefId;
    private Integer statusRefId;
    private Integer jobTypeRefId;
    private Integer priorityRefId;
    private Integer truckMasterRefId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Boolean isActive;
}
