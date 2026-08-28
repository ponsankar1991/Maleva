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
    /** The undecorated account name — legacy's {@code AccountName1}. */
    private String name1;
    private String code;
}

