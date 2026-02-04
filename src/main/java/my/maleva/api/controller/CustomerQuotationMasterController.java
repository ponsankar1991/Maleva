package my.maleva.api.controller;

import my.maleva.api.dto.CustomerQuotationMasterDto;
import my.maleva.api.service.CustomerQuotationMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customer-quotation-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class CustomerQuotationMasterController {

    private final CustomerQuotationMasterService service;

    public CustomerQuotationMasterController(CustomerQuotationMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerQuotationMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CustomerQuotationMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerQuotationMasterDto> create(@Valid @RequestBody CustomerQuotationMasterDto dto) {
        CustomerQuotationMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/customer-quotation-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CustomerQuotationMasterDto update(@PathVariable Integer id, @Valid @RequestBody CustomerQuotationMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
