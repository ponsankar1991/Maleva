package my.maleva.api.module.customer.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerSelectRequest {

    private int companyId;
    private int startIndex;
    private int pageCount;
    private String keyword;
    private String column;
}

