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
public class CustomerQuotationGCDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer customerMasterRefId;

    @NotNull
    private Integer jobMasterRefId;

    @NotNull
    private Integer originRefId;

    @NotNull
    private Integer destinationRefId;

    @NotNull
    private Float threeTon;

    @NotNull
    private Float fiveTon;

    @NotNull
    private Float tenTon;

    @NotNull
    private Float fourtyFeet;

    @NotNull
    private Integer active;

    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @NotNull
    private Float oneTon;
}
