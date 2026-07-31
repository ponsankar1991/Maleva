package my.maleva.api.module.zbentry.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZbEntrySearchRequest {

    @NotNull(message = "CompanyRefId is required")
    private Integer companyRefId;

    private String chargeType;

    private String keyword;

    @NotNull(message = "FromDate is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fromDate;

    @NotNull(message = "ToDate is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate toDate;

}
