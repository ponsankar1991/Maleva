package my.maleva.api.module.joborder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOrderLookupDto {

    private List<LookupItem> statuses;
    private List<LookupItem> jobTypes;
    private List<LookupItem> priorities;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LookupItem {
        private Integer id;
        private String name;
    }
}
