package my.maleva.api.module.master.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for SequenceNoMaster
 * Used for API request/response payloads
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SequenceNoMasterDto {

    private Integer id;

    @NotNull(message = "Company ID is required")
    private Integer companyRefId;

    @NotBlank(message = "Sequence name is required")
    @Size(max = 50, message = "Sequence name cannot exceed 50 characters")
    private String sequenceName;

    private LocalDateTime sequenceDate;

    @NotNull(message = "Sequence number is required")
    private Integer sequenceNo;

    private Integer sequenceYear;

    private Integer sequenceMonth;
}

