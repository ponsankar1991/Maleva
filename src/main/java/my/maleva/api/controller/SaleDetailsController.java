package my.maleva.api.controller;

import my.maleva.api.dto.SaleDetailsDto;
import my.maleva.api.service.SaleDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

/**
 * SaleDetailsController
 * REST Controller for SaleDetails API
 */
@RestController
@RequestMapping("/api/sale-details")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
public class SaleDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(SaleDetailsController.class);

    @Autowired
    private SaleDetailsService saleDetailsService;

    @GetMapping("/sale-master/{saleMasterRefId}")
    public ResponseEntity<List<SaleDetailsDto>> getBySaleMasterRefId(@PathVariable Integer saleMasterRefId) {
        logger.info("Fetching SaleDetails for Sale Master: {}", saleMasterRefId);
        return ResponseEntity.ok(saleDetailsService.getBySaleMasterRefId(saleMasterRefId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Optional<SaleDetailsDto> record = saleDetailsService.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SaleDetailsDto dto) {
        logger.info("Creating new SaleDetails");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(saleDetailsService.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleDetailsDto dto) {
        try {
            return ResponseEntity.ok(saleDetailsService.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        boolean deleted = saleDetailsService.delete(id);
        return deleted ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @GetMapping("/item/{itemMasterRefId}")
    public ResponseEntity<List<SaleDetailsDto>> getByItemMasterRefId(@PathVariable Integer itemMasterRefId) {
        return ResponseEntity.ok(saleDetailsService.getByItemMasterRefId(itemMasterRefId));
    }

    @GetMapping("/count/sale-master/{saleMasterRefId}")
    public ResponseEntity<Long> countBySaleMasterRefId(@PathVariable Integer saleMasterRefId) {
        return ResponseEntity.ok(saleDetailsService.countBySaleMasterRefId(saleMasterRefId));
    }

    @DeleteMapping("/sale-master/{saleMasterRefId}")
    public ResponseEntity<?> deleteAllBySaleMasterRefId(@PathVariable Integer saleMasterRefId) {
        try {
            saleDetailsService.deleteAllBySaleMasterRefId(saleMasterRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}

