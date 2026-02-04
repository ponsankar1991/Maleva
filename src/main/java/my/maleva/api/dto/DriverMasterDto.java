package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotBlank
    @Size(max = 500)
    private String driverName;

    @NotBlank
    @Size(max = 300)
    private String cNumberDisplay;

    @NotNull
    private Integer cNumber;

    @Size(max = 300)
    private String address1;

    @Size(max = 300)
    private String address2;

    @Size(max = 100)
    private String city;

    @Size(max = 50)
    private String zipcode;

    @Size(max = 50)
    private String country;

    @Size(max = 100)
    private String gstno;

    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 50)
    private String mobileNo;

    @Size(max = 50)
    private String userName;

    @Size(max = 50)
    private String password;

    @Size(max = 50)
    private String latitude;

    @Size(max = 50)
    private String longitude;

    @Size(max = 500)
    private String tokenId;

    @NotNull
    private Integer active;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    @Size(max = 50)
    private String licenseNo;

    private LocalDate licenseExp;

    @Size(max = 50)
    private String gdlNo;

    private LocalDate gdlExp;

    @Size(max = 100)
    private String state;

    @Size(max = 300)
    private String address3;

    @Size(max = 100)
    private String personId;

    private LocalDate kuantanPort;
    private LocalDate northportPort;
    private LocalDate pkfzPort;
    private LocalDate kliaPort;
    private LocalDate pguPort;
    private LocalDate tanjungPort;
    private LocalDate penangPort;
    private LocalDate ptpPort;
    private LocalDate westportPort;

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

    private Integer truckRefId;

    @Size(max = 300)
    private String kuantanPortStatus;

    @Size(max = 300)
    private String northportPortStatus;

    @Size(max = 300)
    private String pkfzPortStatus;

    @Size(max = 300)
    private String kliaPortStatus;

    @Size(max = 300)
    private String pguPortStatus;

    @Size(max = 300)
    private String tanjungPortStatus;

    @Size(max = 300)
    private String penangPortStatus;

    @Size(max = 300)
    private String ptpPortStatus;

    @Size(max = 300)
    private String westportPortStatus;

    private LocalDate joiningDate;
    private LocalDate leavingDate;

    @Size(max = 500)
    private String lumutPort;

    @Size(max = 500)
    private String lumutPortStatus;

    @Size(max = 500)
    private String kemamanPort;

    @Size(max = 500)
    private String kemamanPortStatus;
}
