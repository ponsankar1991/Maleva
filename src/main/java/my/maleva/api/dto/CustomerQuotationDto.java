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
public class CustomerQuotationDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer customerMasterRefId;

    @NotNull
    private Integer jobMasterRefId;

    @NotNull
    private Integer itemMasterRefId;

    @NotNull
    private Float mrp;

    @NotNull
    private Float purchaseRate;

    @NotNull
    private Float landingCost;

    @NotNull
    private Float salesRate;

    @NotNull
    private Integer active;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotNull
    private Float start;

    @NotNull
    private Float ends;

    @NotBlank
    @Size(max = 50)
    private String uom;

    @NotBlank
    @Size(max = 50)
    private String port;

    private Integer isTransport;
}
