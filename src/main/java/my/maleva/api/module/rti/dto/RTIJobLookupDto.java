package my.maleva.api.module.rti.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RTIJobLookupDto {

    private Integer id;
    private String jobNo;
    private LocalDateTime jobDate;
    private String customerName;
}