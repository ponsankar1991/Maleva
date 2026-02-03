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
public class AddressMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 1000)
    private String address;

    @Size(max = 50)
    private String phone;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    @NotNull
    private Integer active;
}
