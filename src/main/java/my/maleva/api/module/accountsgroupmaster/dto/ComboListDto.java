package my.maleva.api.module.accountsgroupmaster.dto;

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
    private String name;
    private String code;
}

