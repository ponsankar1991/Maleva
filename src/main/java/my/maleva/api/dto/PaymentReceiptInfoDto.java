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
public class PaymentReceiptInfoDto {
    private Integer id;

    @NotNull
    @Size(max = 255)
    private String companyName;

    @Size(max = 100)
    private String registrationNo;

    @Size(max = 100)
    private String gstRegNo;

    @Size(max = 500)
    private String address;

    @Size(max = 50)
    private String phone;

    @Size(max = 100)
    private String selfBilledType;

    @Size(max = 100)
    private String tinType;

    @Size(max = 100)
    private String supplierTIN;

    @Size(max = 100)
    private String msicCode;

    @Size(max = 500)
    private String address1;

    @Size(max = 20)
    private String zipCode;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Size(max = 100)
    private String salesTaxRegNo;

    @Size(max = 100)
    private String serviceTaxRegNo;

    @Size(max = 100)
    private String tourismTaxRegNo;

    private LocalDateTime createdDate;

    @NotNull
    private Integer active;

    @NotNull
    private Integer eInvoice;

    @Size(max = 50)
    private String stateCode;

    private Integer countryId;

    private Integer msicCodeInt;

    @Size(max = 100)
    private String email;
}
