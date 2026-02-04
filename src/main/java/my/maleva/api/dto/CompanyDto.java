package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {
    private Integer id;

    @NotNull
    private Integer cCode;

    @NotBlank
    @Size(max = 200)
    private String cName;

    @NotNull
    private Boolean cstatus;

    @NotNull
    private Boolean active;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    @Size(max = 100)
    private String computerName;

    @Size(max = 200)
    private String branchName;

    @NotNull
    private LocalDate fromDate;

    @NotNull
    private LocalDate toDate;

    private Integer mComid;

    @Size(max = 50)
    private String mobileNo;

    private Integer parentId;

    @Size(max = 50)
    private String username;

    private UUID webCode;

    @Size(max = 500)
    private String tokenId;

    private Integer webId;

    @Size(max = 70)
    private String ownerName;

    @Size(max = 70)
    private String address1;

    @Size(max = 70)
    private String address2;

    @Size(max = 70)
    private String city;

    @Size(max = 70)
    private String landMark;

    @Size(max = 10)
    private String pincode;

    @Size(max = 20)
    private String mobileNo1;

    @Size(max = 20)
    private String mobileNo2;

    @Size(max = 100)
    private String latitude;

    @Size(max = 100)
    private String longitude;

    private Float margin;

    @Size(max = 100)
    private String areaName;

    @Email
    @Size(max = 50)
    private String email;

    @Size(max = 100)
    private String imagePath;

    @Size(max = 100)
    private String upiId;

    @Size(max = 50)
    private String password;
}
