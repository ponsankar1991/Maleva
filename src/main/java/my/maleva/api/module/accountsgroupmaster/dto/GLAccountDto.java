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
public class GLAccountDto {

    private UUID id;
    private Integer companyRefId;
    private String glAccountCode;
    private String description;
    private String accountName1;
    private UUID parentId;
    private Integer isActive;
    private Integer classification;
}

