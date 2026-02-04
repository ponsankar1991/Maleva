package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngineHoursDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer truckRefId;

    @NotNull
    private LocalDateTime beginTime;

    @NotNull
    private LocalDateTime endTime;

    @Size(max = 800)
    private String beginLocation;

    @Size(max = 800)
    private String endLocation;

    @Size(max = 100)
    private String totalTime;

    @Size(max = 100)
    private String inMotion;

    @Size(max = 100)
    private String idling;

    @Size(max = 100)
    private String mileage;

    @Size(max = 100)
    private String consumedbyFLSinidlerun;

    private LocalDateTime createdDate;
}
