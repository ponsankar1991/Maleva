package my.maleva.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerJobNotifyDto {
    private Integer id;

    @NotNull
    private Integer companyRefId;

    @NotNull
    private Integer customerDetailRefId;

    @NotNull
    private Integer saleOrderRefId;

    @NotNull
    private Integer whatsapp;

    @NotNull
    private Integer email;

    @Size(max = 200)
    private String phone;
}
