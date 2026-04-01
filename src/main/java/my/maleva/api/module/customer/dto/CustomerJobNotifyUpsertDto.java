package my.maleva.api.module.customer.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerJobNotifyUpsertDto {

    @JsonAlias("Name")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @JsonAlias("Id")
    @Min(value = 0, message = "Id must be zero or a positive integer")
    private Integer id;

    @JsonAlias("CustomerDetailRefId")
    @NotNull(message = "CustomerDetailRefId is required")
    @Min(value = 1, message = "CustomerDetailRefId must be a positive integer")
    private Integer customerDetailRefId;

    @JsonAlias("SaleOrderRefId")
    @NotNull(message = "SaleOrderRefId is required")
    @Min(value = 1, message = "SaleOrderRefId must be a positive integer")
    private Integer saleOrderRefId;

    @JsonAlias("Whatsapp")
    @NotNull(message = "Whatsapp is required")
    @Min(value = 0, message = "Whatsapp must be zero or a positive integer")
    private Integer whatsapp;

    @JsonAlias("Email")
    @NotNull(message = "Email is required")
    @Min(value = 0, message = "Email must be zero or a positive integer")
    private Integer email;

    @JsonAlias("Phone")
    @Min(value = 0, message = "Phone must be zero or a positive integer")
    private Integer phone;

    @JsonAlias("WhatsappDisplay")
    @Size(max = 200, message = "WhatsappDisplay must not exceed 200 characters")
    private String whatsappDisplay;

    @JsonAlias("EmailDisplay")
    @Size(max = 200, message = "EmailDisplay must not exceed 200 characters")
    private String emailDisplay;

    @JsonAlias("PhoneDisplay")
    @Size(max = 200, message = "PhoneDisplay must not exceed 200 characters")
    private String phoneDisplay;
}
