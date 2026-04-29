package my.maleva.api.module.prealert.service;

import my.maleva.api.module.prealert.dto.PreAlertDto;

import java.util.List;

public interface PreAlertService {

    /**
     * Get all PreAlert records by PreAlertMaster ID
     */
    List<PreAlertDto> getByPreAlertMasterId(Integer preAlertMasterRefId);

    /**
     * Get count of records by PreAlertMaster ID
     */
    Long countByPreAlertMasterId(Integer preAlertMasterRefId);
}
