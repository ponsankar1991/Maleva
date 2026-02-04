package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GLAccountsDto {
    private UUID id;

    @NotNull
    private Integer companyRefId;

    private UUID parentId;

    @NotBlank
    @Size(max = 20)
    private String glAccountCode;

    private UUID accountId;
    private Integer specialAccountId;
    private Integer currencyId;
    private Integer gstTypeId;

    @NotBlank
    @Size(max = 100)
    private String description;

    @Size(max = 2)
    private String drCr;

    private Boolean isCreditCard;

    @NotNull
    private Boolean isActive;

    @Size(max = 1)
    private String gstGroup;

    private Boolean isRevaluation;

    private String notes;
    private Boolean isSubAccount;

    @Size(max = 100)
    private String bankAccountNo;

    @Size(max = 10)
    private String gstMsicCode;

    private Integer optimisticLockField;

    @Size(max = 20)
    private String sac;

    @Size(max = 12)
    private String sstTariffCode;

    @NotNull
    private Integer rowIndex;

    private Boolean hasChildInCoa;
    private Boolean includeInCashFlowForecastAdvisor;

    private Integer tariffCodeId;

    private UUID atcCodeId;

    @Size(max = 100)
    private String description2;

    private Integer classification;
}
