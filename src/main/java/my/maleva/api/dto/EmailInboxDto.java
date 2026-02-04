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
public class EmailInboxDto {
    private Integer id;

    @Size(max = 200)
    private String emailId;

    @NotBlank
    @Size(max = 500)
    private String subject;

    @NotBlank
    @Email
    @Size(max = 255)
    private String sender;

    @NotNull
    private LocalDateTime receivedDate;

    @NotNull
    private Integer isUnread;

    @NotNull
    private Integer active;

    @NotNull
    private Integer isReplied;

    private Integer employeeRefId;
}
