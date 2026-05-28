package my.maleva.api.module.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboListDto {

    private Integer id;

    private String accountName;

    private String accountName1;

    private String accountCode;

    private String employeeType;

    private String password;

    private String oEmail1;
}