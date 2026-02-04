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
public class FuelFillingsDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer truckRefId;

    @Size(max = 200)
    private String vehicle;

    @NotNull
    private LocalDateTime time;

    @Size(max = 200)
    private String location;

    @Size(max = 100)
    private String count;

    @Size(max = 100)
    private String filled;

    @Size(max = 200)
    private String driver;

    private LocalDateTime createdDate;
}
