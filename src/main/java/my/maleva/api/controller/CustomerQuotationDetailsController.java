package my.maleva.api.controller;

import my.maleva.api.dto.CustomerQuotationDetailsDto;
import my.maleva.api.service.CustomerQuotationDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customer-quotation-details")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class CustomerQuotationDetailsController {

    private final CustomerQuotationDetailsService service;

    public CustomerQuotationDetailsController(CustomerQuotationDetailsService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerQuotationDetailsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CustomerQuotationDetailsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerQuotationDetailsDto> create(@Valid @RequestBody CustomerQuotationDetailsDto dto) {
        CustomerQuotationDetailsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/customer-quotation-details/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CustomerQuotationDetailsDto update(@PathVariable Integer id, @Valid @RequestBody CustomerQuotationDetailsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
