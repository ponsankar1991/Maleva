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
public class CustomerNotifyDetailsDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer customerMasterRefId;

    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String whatsapp;

    @Email
    @Size(max = 200)
    private String email;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
