package my.maleva.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerInsertResult {

    private boolean success;
    private String message;
    private String accountName;
    private Integer customerId;
}

