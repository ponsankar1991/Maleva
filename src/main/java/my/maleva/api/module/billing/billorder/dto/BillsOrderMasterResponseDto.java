package my.maleva.api.module.billing.billorder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Response DTO for BillsOrderMaster insert/update operation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillsOrderMasterResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer result;

    private String message;

    private String billNo;

    private LocalDateTime saleTime;

    private Integer id;

    private String accountName;

    /**
     * Indicates if the operation was successful
     */
    public boolean isSuccess() {
        return result != null && result == 1;
    }
}

