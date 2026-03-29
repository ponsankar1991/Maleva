package my.maleva.api.module.saleorder.controller;

import my.maleva.api.module.saleorder.dto.SaleOrderDetailsDto;
import my.maleva.api.module.saleorder.service.SaleOrderDetailsService;
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
 * SaleOrderDetailsController - REST Controller for SaleOrderDetails API
 */
@RestController
@RequestMapping("/api/sale-order-details")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class SaleOrderDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderDetailsController.class);

    @Autowired
    private SaleOrderDetailsService service;

    @GetMapping("/sale-order-master/{saleOrderMasterRefId}")
    public ResponseEntity<List<SaleOrderDetailsDto>> getBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        logger.info("Fetching SaleOrderDetails for order: {}", saleOrderMasterRefId);
        return ResponseEntity.ok(service.getBySaleOrderMasterRefId(saleOrderMasterRefId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Optional<SaleOrderDetailsDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SaleOrderDetailsDto dto) {
        logger.info("Creating SaleOrderDetails");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleOrderDetailsDto dto) {
        try {
            return ResponseEntity.ok(service.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        return service.delete(id) ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @GetMapping("/item/{itemMasterRefId}")
    public ResponseEntity<List<SaleOrderDetailsDto>> getByItemMasterRefId(@PathVariable Integer itemMasterRefId) {
        return ResponseEntity.ok(service.getByItemMasterRefId(itemMasterRefId));
    }

    @GetMapping("/count/sale-order-master/{saleOrderMasterRefId}")
    public ResponseEntity<Long> countBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        return ResponseEntity.ok(service.countBySaleOrderMasterRefId(saleOrderMasterRefId));
    }

    @DeleteMapping("/sale-order-master/{saleOrderMasterRefId}")
    public ResponseEntity<?> deleteAllBySaleOrderMasterRefId(@PathVariable Integer saleOrderMasterRefId) {
        try {
            service.deleteAllBySaleOrderMasterRefId(saleOrderMasterRefId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}

