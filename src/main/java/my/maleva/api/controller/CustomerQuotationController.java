package my.maleva.api.controller;

import my.maleva.api.dto.CustomerQuotationDto;
import my.maleva.api.service.CustomerQuotationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customer-quotations")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class CustomerQuotationController {

    private final CustomerQuotationService service;

    public CustomerQuotationController(CustomerQuotationService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerQuotationDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CustomerQuotationDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerQuotationDto> create(@Valid @RequestBody CustomerQuotationDto dto) {
        CustomerQuotationDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/customer-quotations/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CustomerQuotationDto update(@PathVariable Integer id, @Valid @RequestBody CustomerQuotationDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
