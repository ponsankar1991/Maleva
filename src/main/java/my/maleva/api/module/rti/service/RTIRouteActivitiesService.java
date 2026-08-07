package my.maleva.api.module.rti.service;

import my.maleva.api.module.rti.dto.RTIRouteActivitiesDto;
import java.util.List;

public interface RTIRouteActivitiesService {
    List<RTIRouteActivitiesDto> getByRtiMasterId(Integer rtiMasterRefId);
}
