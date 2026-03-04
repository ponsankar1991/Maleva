package my.maleva.api.controller;

import my.maleva.api.dto.SaleOrderDTO;
import my.maleva.api.dto.SaleOrderMasterDto;
import my.maleva.api.service.SaleOrderMasterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * SaleOrderMasterController - REST Controller for SaleOrderMaster API
 */
@RestController
@RequestMapping("/api/sale-orders")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPERADMIN')")
public class SaleOrderMasterController {

    private static final Logger logger = LoggerFactory.getLogger(SaleOrderMasterController.class);

    @Autowired
    private SaleOrderMasterService service;

    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<SaleOrderMasterDto>> getAllByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Fetching all SaleOrderMaster records for company: {}", companyRefId);
        return ResponseEntity.ok(service.getAllByCompanyId(companyRefId));
    }

    @GetMapping("/company/{companyRefId}/status/{active}")
    public ResponseEntity<List<SaleOrderMasterDto>> getByCompanyIdAndStatus(@PathVariable Integer companyRefId, @PathVariable Integer active) {
        return ResponseEntity.ok(service.getByCompanyIdAndStatus(companyRefId, active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Optional<SaleOrderMasterDto> record = service.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SaleOrderMasterDto dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: " + e.getMessage());
        }
    }
    @PostMapping("/save")
    public ResponseEntity<?> save(@Valid @RequestBody SaleOrderDTO dto) {
        try {
            logger.info("Saving SaleOrder for company: {} customer: {} cNumber: {}",
                       dto.getCompanyRefId(), dto.getCustomerRefId(), dto.getCNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(service.save(dto));
        } catch (RuntimeException e) {
            logger.error("Error saving SaleOrder - {}. Check that cNumber, billType, saleType are not null/empty",
                        e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error saving SaleOrder: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Unexpected error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleOrderMasterDto dto) {
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

    @GetMapping("/customer/{customerRefId}")
    public ResponseEntity<List<SaleOrderMasterDto>> getByCustomerRefId(@PathVariable Integer customerRefId) {
        return ResponseEntity.ok(service.getByCustomerRefId(customerRefId));
    }

    @GetMapping("/company/{companyRefId}/customer/{customerRefId}")
    public ResponseEntity<List<SaleOrderMasterDto>> getByCompanyAndCustomer(@PathVariable Integer companyRefId, @PathVariable Integer customerRefId) {
        return ResponseEntity.ok(service.getByCompanyAndCustomer(companyRefId, customerRefId));
    }

    @GetMapping("/company/{companyRefId}/c-number/{cNumber}")
    public ResponseEntity<?> getByCNumber(@PathVariable Integer companyRefId, @PathVariable Integer cNumber) {
        Optional<SaleOrderMasterDto> record = service.getByCNumber(companyRefId, cNumber);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @GetMapping("/company/{companyRefId}/date-range")
    public ResponseEntity<?> getByDateRange(
            @PathVariable Integer companyRefId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            return ResponseEntity.ok(service.getByDateRange(companyRefId, startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date range");
        }
    }

    @GetMapping("/employee/{employeeRefId}")
    public ResponseEntity<List<SaleOrderMasterDto>> getByEmployeeId(@PathVariable Integer employeeRefId) {
        return ResponseEntity.ok(service.getByEmployeeId(employeeRefId));
    }

    @GetMapping("/company/{companyRefId}/employee/{employeeRefId}")
    public ResponseEntity<List<SaleOrderMasterDto>> getByCompanyAndEmployee(@PathVariable Integer companyRefId, @PathVariable Integer employeeRefId) {
        return ResponseEntity.ok(service.getByCompanyAndEmployee(companyRefId, employeeRefId));
    }

    @GetMapping("/user/{userRefId}")
    public ResponseEntity<List<SaleOrderMasterDto>> getByUserId(@PathVariable Integer userRefId) {
        return ResponseEntity.ok(service.getByUserId(userRefId));
    }

    @GetMapping("/job/{jobMasterRefId}")
    public ResponseEntity<List<SaleOrderMasterDto>> getByJobMasterRefId(@PathVariable Integer jobMasterRefId) {
        return ResponseEntity.ok(service.getByJobMasterRefId(jobMasterRefId));
    }

    @GetMapping("/agent/{agentMasterRefId}")
    public ResponseEntity<List<SaleOrderMasterDto>> getByAgentMasterRefId(@PathVariable Integer agentMasterRefId) {
        return ResponseEntity.ok(service.getByAgentMasterRefId(agentMasterRefId));
    }

    @GetMapping("/driver/{driverRefid}")
    public ResponseEntity<List<SaleOrderMasterDto>> getByDriverRefid(@PathVariable Integer driverRefid) {
        return ResponseEntity.ok(service.getByDriverRefid(driverRefid));
    }

    @GetMapping("/count/company/{companyRefId}")
    public ResponseEntity<Long> countByCompanyId(@PathVariable Integer companyRefId) {
        return ResponseEntity.ok(service.countByCompanyId(companyRefId));
    }

    @GetMapping("/count/company/{companyRefId}/status/{active}")
    public ResponseEntity<Long> countByCompanyIdAndStatus(@PathVariable Integer companyRefId, @PathVariable Integer active) {
        return ResponseEntity.ok(service.countByCompanyIdAndStatus(companyRefId, active));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.activate(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.deactivate(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }
}

