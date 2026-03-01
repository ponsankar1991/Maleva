package my.maleva.api.controller;

import my.maleva.api.dto.SaleMasterDto;
import my.maleva.api.service.SaleMasterService;
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
 * SaleMasterController
 * REST Controller for SaleMaster API
 */
@RestController
@RequestMapping("/api/sales")
@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_SUPRERADMIN')")
public class SaleMasterController {

    private static final Logger logger = LoggerFactory.getLogger(SaleMasterController.class);

    @Autowired
    private SaleMasterService saleMasterService;

    @GetMapping("/company/{companyRefId}")
    public ResponseEntity<List<SaleMasterDto>> getAllByCompanyId(@PathVariable Integer companyRefId) {
        logger.info("Fetching all SaleMaster records for company: {}", companyRefId);
        return ResponseEntity.ok(saleMasterService.getAllByCompanyId(companyRefId));
    }

    @GetMapping("/company/{companyRefId}/status/{active}")
    public ResponseEntity<List<SaleMasterDto>> getByCompanyIdAndStatus(@PathVariable Integer companyRefId, @PathVariable Integer active) {
        logger.info("Fetching SaleMaster records for company: {} and status: {}", companyRefId, active);
        return ResponseEntity.ok(saleMasterService.getByCompanyIdAndStatus(companyRefId, active));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        logger.info("Fetching SaleMaster by ID: {}", id);
        Optional<SaleMasterDto> record = saleMasterService.getById(id);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleMaster not found with ID: " + id);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SaleMasterDto dto) {
        logger.info("Creating new SaleMaster for company: {}", dto.getCompanyRefId());
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(saleMasterService.create(dto));
        } catch (RuntimeException e) {
            logger.error("Error creating SaleMaster: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody SaleMasterDto dto) {
        logger.info("Updating SaleMaster with ID: {}", id);
        try {
            return ResponseEntity.ok(saleMasterService.update(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleMaster not found with ID: " + id);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        logger.info("Deleting SaleMaster with ID: {}", id);
        boolean deleted = saleMasterService.delete(id);
        return deleted ? ResponseEntity.noContent().build() :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("SaleMaster not found with ID: " + id);
    }

    @GetMapping("/customer/{customerRefId}")
    public ResponseEntity<List<SaleMasterDto>> getByCustomerRefId(@PathVariable Integer customerRefId) {
        return ResponseEntity.ok(saleMasterService.getByCustomerRefId(customerRefId));
    }

    @GetMapping("/company/{companyRefId}/customer/{customerRefId}")
    public ResponseEntity<List<SaleMasterDto>> getByCompanyAndCustomer(@PathVariable Integer companyRefId, @PathVariable Integer customerRefId) {
        return ResponseEntity.ok(saleMasterService.getByCompanyAndCustomer(companyRefId, customerRefId));
    }

    @GetMapping("/company/{companyRefId}/c-number/{cNumber}")
    public ResponseEntity<?> getByCNumber(@PathVariable Integer companyRefId, @PathVariable Integer cNumber) {
        Optional<SaleMasterDto> record = saleMasterService.getByCNumber(companyRefId, cNumber);
        return record.isPresent() ? ResponseEntity.ok(record.get()) :
               ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }

    @GetMapping("/company/{companyRefId}/date-range")
    public ResponseEntity<?> getByDateRange(
            @PathVariable Integer companyRefId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            return ResponseEntity.ok(saleMasterService.getByDateRange(companyRefId, startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date range");
        }
    }

    @GetMapping("/employee/{employeeRefId}")
    public ResponseEntity<List<SaleMasterDto>> getByEmployeeId(@PathVariable Integer employeeRefId) {
        return ResponseEntity.ok(saleMasterService.getByEmployeeId(employeeRefId));
    }

    @GetMapping("/company/{companyRefId}/employee/{employeeRefId}")
    public ResponseEntity<List<SaleMasterDto>> getByCompanyAndEmployee(@PathVariable Integer companyRefId, @PathVariable Integer employeeRefId) {
        return ResponseEntity.ok(saleMasterService.getByCompanyAndEmployee(companyRefId, employeeRefId));
    }

    @GetMapping("/user/{userRefId}")
    public ResponseEntity<List<SaleMasterDto>> getByUserId(@PathVariable Integer userRefId) {
        return ResponseEntity.ok(saleMasterService.getByUserId(userRefId));
    }

    @GetMapping("/company/{companyRefId}/bill-type/{billType}")
    public ResponseEntity<List<SaleMasterDto>> getByCompanyAndBillType(@PathVariable Integer companyRefId, @PathVariable String billType) {
        return ResponseEntity.ok(saleMasterService.getByCompanyAndBillType(companyRefId, billType));
    }

    @GetMapping("/company/{companyRefId}/sale-type/{saleType}")
    public ResponseEntity<List<SaleMasterDto>> getByCompanyAndSaleType(@PathVariable Integer companyRefId, @PathVariable String saleType) {
        return ResponseEntity.ok(saleMasterService.getByCompanyAndSaleType(companyRefId, saleType));
    }

    @GetMapping("/job/{jobMasterRefId}")
    public ResponseEntity<List<SaleMasterDto>> getByJobMasterRefId(@PathVariable Integer jobMasterRefId) {
        return ResponseEntity.ok(saleMasterService.getByJobMasterRefId(jobMasterRefId));
    }

    @GetMapping("/agent/{agentMasterRefId}")
    public ResponseEntity<List<SaleMasterDto>> getByAgentMasterRefId(@PathVariable Integer agentMasterRefId) {
        return ResponseEntity.ok(saleMasterService.getByAgentMasterRefId(agentMasterRefId));
    }

    @GetMapping("/driver/{driverRefid}")
    public ResponseEntity<List<SaleMasterDto>> getByDriverRefid(@PathVariable Integer driverRefid) {
        return ResponseEntity.ok(saleMasterService.getByDriverRefid(driverRefid));
    }

    @GetMapping("/count/company/{companyRefId}")
    public ResponseEntity<Long> countByCompanyId(@PathVariable Integer companyRefId) {
        return ResponseEntity.ok(saleMasterService.countByCompanyId(companyRefId));
    }

    @GetMapping("/count/company/{companyRefId}/status/{active}")
    public ResponseEntity<Long> countByCompanyIdAndStatus(@PathVariable Integer companyRefId, @PathVariable Integer active) {
        return ResponseEntity.ok(saleMasterService.countByCompanyIdAndStatus(companyRefId, active));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(saleMasterService.activate(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(saleMasterService.deactivate(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }
}

