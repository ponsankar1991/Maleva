package my.maleva.api.controller;

import my.maleva.api.dto.CustomerQuotationGCDto;
import my.maleva.api.service.CustomerQuotationGCService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customer-quotation-gc")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class CustomerQuotationGCController {

    private final CustomerQuotationGCService service;

    public CustomerQuotationGCController(CustomerQuotationGCService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerQuotationGCDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CustomerQuotationGCDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerQuotationGCDto> create(@Valid @RequestBody CustomerQuotationGCDto dto) {
        CustomerQuotationGCDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/customer-quotation-gc/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CustomerQuotationGCDto update(@PathVariable Integer id, @Valid @RequestBody CustomerQuotationGCDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
