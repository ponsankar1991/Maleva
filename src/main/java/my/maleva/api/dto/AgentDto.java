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
public class AgentDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer agentCompanyRefId;

    @NotBlank
    @Size(max = 500)
    private String agentName;

    @NotBlank
    @Size(max = 300)
    private String cNumberDisplay;

    @NotNull
    private Integer cNumber;

    @Size(max = 300)
    private String address1;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 50)
    private String mobileNo;

    @Size(max = 50)
    private String userName;

    @Size(max = 50)
    private String password;

    @Size(max = 500)
    private String tokenId;

    @NotNull
    private Integer active;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    @NotNull
    private Integer accountRefid;

    @Size(max = 100)
    private String tinNo;

    @Size(max = 100)
    private String sstNo;

    @Size(max = 100)
    private String msicCode;

    @Size(max = 100)
    private String serviceTaxType;

    @Size(max = 100)
    private String bankName;

    @Size(max = 100)
    private String accountNo;
}
