package my.maleva.api.module.rti.controller;

import my.maleva.api.module.rti.dto.RTIDetailsDto;
import my.maleva.api.module.rti.service.RTIDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

/**
 * RTIDetails REST Controller
 * Handles all RESTful API endpoints for RTIDetails operations
 * Base URL: /api/rti-details
 */
@RestController
@RequestMapping("/api/rti-details")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RTIDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(RTIDetailsController.class);

    @Autowired
    private RTIDetailsService rtiDetailsService;

    @GetMapping("/rti-master/{rtiMasterRefId}")
    @PermitAll
    public ResponseEntity<List<RTIDetailsDto>> getByRtiMasterId(@PathVariable Integer rtiMasterRefId) {
        logger.info("Fetching RTIDetails for RTIMaster: {}", rtiMasterRefId);
        List<RTIDetailsDto> records = rtiDetailsService.getByRtiMasterId(rtiMasterRefId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching RTIDetails by ID: {}", id);
        Optional<RTIDetailsDto> record = rtiDetailsService.getById(id);
        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RTIDetails not found with ID: " + id);
        }
    }

    @PostMapping
    @PermitAll
    public ResponseEntity<?> create(@Valid @RequestBody RTIDetailsDto dto) {
        logger.info("Creating new RTIDetails for RTIMaster: {}", dto.getRtiMasterRefId());
        try {
            RTIDetailsDto created = rtiDetailsService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.error("Error creating RTIDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating RTIDetails: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody RTIDetailsDto dto) {
        logger.info("Updating RTIDetails with ID: {}", id);
        try {
            RTIDetailsDto updated = rtiDetailsService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            logger.error("RTIDetails not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RTIDetails not found with ID: " + id);
        } catch (Exception e) {
            logger.error("Error updating RTIDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating RTIDetails: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PermitAll
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting RTIDetails with ID: {}", id);
        try {
            boolean deleted = rtiDetailsService.delete(id);
            if (deleted) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("RTIDetails not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting RTIDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting RTIDetails: " + e.getMessage());
        }
    }

    @GetMapping("/sale-order/{saleOrderMasterRefId}")
    @PermitAll
    public ResponseEntity<List<RTIDetailsDto>> getBySaleOrderMasterId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Fetching RTIDetails by sale order master: {}", saleOrderMasterRefId);
        List<RTIDetailsDto> records = rtiDetailsService.getBySaleOrderMasterId(saleOrderMasterRefId);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/rti-master/{rtiMasterRefId}/count")
    @PermitAll
    public ResponseEntity<?> countByRtiMasterId(@PathVariable Integer rtiMasterRefId) {
        logger.info("Counting RTIDetails for RTIMaster: {}", rtiMasterRefId);
        long count = rtiDetailsService.countByRtiMasterId(rtiMasterRefId);
        return ResponseEntity.ok("Total: " + count);
    }

    @DeleteMapping("/rti-master/{rtiMasterRefId}")
    @PermitAll
    public ResponseEntity<?> deleteByRtiMasterId(@PathVariable Integer rtiMasterRefId) {
        logger.info("Deleting all RTIDetails for RTIMaster: {}", rtiMasterRefId);
        try {
            rtiDetailsService.deleteByRtiMasterId(rtiMasterRefId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            logger.error("Error deleting RTIDetails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting RTIDetails: " + e.getMessage());
        }
    }
}


