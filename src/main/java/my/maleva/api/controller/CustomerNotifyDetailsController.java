package my.maleva.api.controller;

import my.maleva.api.dto.CustomerNotifyDetailsDto;
import my.maleva.api.service.CustomerNotifyDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customer-notify-details")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class CustomerNotifyDetailsController {

    private final CustomerNotifyDetailsService service;

    public CustomerNotifyDetailsController(CustomerNotifyDetailsService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerNotifyDetailsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CustomerNotifyDetailsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerNotifyDetailsDto> create(@Valid @RequestBody CustomerNotifyDetailsDto dto) {
        CustomerNotifyDetailsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/customer-notify-details/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CustomerNotifyDetailsDto update(@PathVariable Integer id, @Valid @RequestBody CustomerNotifyDetailsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
