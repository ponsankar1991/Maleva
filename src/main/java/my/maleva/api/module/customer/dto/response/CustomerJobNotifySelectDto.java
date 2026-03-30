package my.maleva.api.module.customer.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerJobNotifySelectDto {

    private Integer id;
    private String name;
    private Integer customerDetailRefId;
    private Integer whatsapp;
    private Integer phone;
    private Integer email;
    private String whatsappDisplay;
    private String phoneDisplay;
    private String emailDisplay;
}
