package my.maleva.api.module.employee.service;

import my.maleva.api.module.employee.dto.CapabilityDto;
import java.util.List;

public interface CapabilityService {
    List<CapabilityDto> getActiveCapabilities();
}
