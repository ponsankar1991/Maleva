package my.maleva.api.module.zbentry.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZbEntryBulkSaveRequest {

    @NotNull(message = "CompanyRefId is required")
    private Integer companyRefId;

    @NotEmpty(message = "Details list cannot be empty")
    @Valid
    private List<ZbEntrySaveRequest> details;
}
