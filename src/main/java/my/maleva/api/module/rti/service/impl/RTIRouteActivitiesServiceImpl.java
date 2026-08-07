package my.maleva.api.module.rti.service.impl;

import my.maleva.api.module.rti.dto.RTIRouteActivitiesDto;
import my.maleva.api.module.rti.mapper.RTIRouteActivitiesMapper;
import my.maleva.api.module.rti.repository.RTIRouteActivitiesRepository;
import my.maleva.api.module.rti.service.RTIRouteActivitiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RTIRouteActivitiesServiceImpl implements RTIRouteActivitiesService {

    private static final Logger logger = LoggerFactory.getLogger(RTIRouteActivitiesServiceImpl.class);

    @Autowired
    private RTIRouteActivitiesRepository repository;

    @Autowired
    private RTIRouteActivitiesMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<RTIRouteActivitiesDto> getByRtiMasterId(Integer rtiMasterRefId) {
        logger.info("Fetching RTIRouteActivities for RTIMaster: {}", rtiMasterRefId);
        return repository.findByRtiMasterRefIdOrderBySequenceNoAsc(rtiMasterRefId)
                .stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
