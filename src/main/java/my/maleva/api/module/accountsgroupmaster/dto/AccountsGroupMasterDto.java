package my.maleva.api.module.accountsgroupmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsGroupMasterDto {

    private Integer id;
    private Integer companyRefId;
    private String accountCode;
    private String accountName;
    private String accountName1;
    private Integer parentId;
    private Integer editmode;
    private Integer noChild;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String modifiedBy;
    private Integer active;
    private String qneCode;
    private String updateId;
}

