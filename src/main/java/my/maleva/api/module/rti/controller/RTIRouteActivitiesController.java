package my.maleva.api.module.rti.controller;

import my.maleva.api.module.rti.dto.RTIRouteActivitiesDto;
import my.maleva.api.module.rti.service.RTIRouteActivitiesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@RestController
@RequestMapping("/api/rti-route-activities")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RTIRouteActivitiesController {

    private static final Logger logger = LoggerFactory.getLogger(RTIRouteActivitiesController.class);

    @Autowired
    private RTIRouteActivitiesService service;

    @GetMapping("/rti-master/{rtiMasterRefId}")
    @PermitAll
    public ResponseEntity<List<RTIRouteActivitiesDto>> getByRtiMasterId(@PathVariable Integer rtiMasterRefId) {
        logger.info("Fetching RTIRouteActivities for RTIMaster: {}", rtiMasterRefId);
        List<RTIRouteActivitiesDto> records = service.getByRtiMasterId(rtiMasterRefId);
        return ResponseEntity.ok(records);
    }
}
