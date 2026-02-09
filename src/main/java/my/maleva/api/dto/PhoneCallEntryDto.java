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
public class PhoneCallEntryDto {
    private Integer id;

    @NotNull
    private LocalDateTime callDate;

    @NotBlank
    @Size(max = 50)
    private String phoneNo;

    @NotBlank
    @Size(max = 1500)
    private String remarks;
}
