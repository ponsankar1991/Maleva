package my.maleva.api.dto.response;



import lombok.AllArgsConstructor;
import lombok.Getter;


import java.util.List;

@Getter
@AllArgsConstructor
public class CustomerSelectResult {

    private List<CustomerSelectDto> customers;
    private long totalCount;
}

