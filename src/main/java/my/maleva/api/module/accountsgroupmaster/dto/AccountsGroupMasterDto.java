package my.maleva.api.module.accountsgroupmaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsGroupMasterDto {

    private Integer id;
    private Integer companyRefId;
    private Integer classification;
    private String accountCode;
    private String classificationName;
    private String accountName;
    private String accountName1;
    private String parentName;
    private UUID parentId;
    private UUID rootId;
    private Integer active;
    private String qneCode;
    private String updateId;
}

