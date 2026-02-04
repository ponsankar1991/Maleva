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
public class ItemMasterDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotBlank
    @Size(max = 50)
    private String prodCode;

    @NotNull
    private Integer pcodeDigits;

    @NotBlank
    @Size(max = 100)
    private String pName;

    @Size(max = 100)
    private String printName;

    @Size(max = 100)
    private String secondPCode;

    @Size(max = 100)
    private String hsnCode;

    @NotNull
    private Integer taxCode;

    @NotNull
    private Integer uomCode;

    @NotNull
    private Float mrp;

    @NotNull
    private Float purchaseRate;

    @NotNull
    private Float landingCost;

    @NotNull
    private Float salesRate;

    @NotNull
    private Boolean saleRateType;

    @Size(max = 100)
    private String remarks;

    @NotNull
    private Integer activestatus;

    @NotNull
    private Integer sorting;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotBlank
    @Size(max = 50)
    private String modifiedBy;

    @Size(max = 50)
    private String qneCode;

    @Size(max = 50)
    private String qneId;

    @Size(max = 250)
    private String eInvoice;

    private Integer saleClassification;
    private Integer selfBilledClassification;
}
