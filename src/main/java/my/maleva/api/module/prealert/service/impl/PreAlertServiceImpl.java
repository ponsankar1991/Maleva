package my.maleva.api.module.prealert.service.impl;

import my.maleva.api.module.prealert.dto.PreAlertDto;
import my.maleva.api.module.prealert.mapper.PreAlertMapper;
import my.maleva.api.module.prealert.repository.PreAlertRepository;
import my.maleva.api.module.prealert.service.PreAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PreAlert Service Implementation
 * Handles read-only access to PreAlert detail records for master-scoped endpoints.
 */
@Service
public class PreAlertServiceImpl implements PreAlertService {

    private static final Logger logger = LoggerFactory.getLogger(PreAlertServiceImpl.class);

    @Autowired
    private PreAlertRepository preAlertRepository;

    @Autowired
    private PreAlertMapper mapper;

    @Override
    public List<PreAlertDto> getByPreAlertMasterId(Integer preAlertMasterRefId) {
        logger.info("Fetching PreAlert records for master: {}", preAlertMasterRefId);
        return preAlertRepository.findByPreAlertMasterRefId(preAlertMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Long countByPreAlertMasterId(Integer preAlertMasterRefId) {
        logger.info("Counting PreAlert records for master: {}", preAlertMasterRefId);
        return preAlertRepository.countByPreAlertMasterRefId(preAlertMasterRefId);
    }
}
