package my.maleva.api.module.customer.dto.response;

import my.maleva.api.module.customer.dto.CustomerJobNotifyDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerJobNotifyBulkResultDto {
    private Integer savedCount;
    private Integer lastSavedId;
    private List<CustomerJobNotifyDto> notifications;
}
