package my.maleva.api.controller;

import my.maleva.api.dto.CustomerJobNotifyDto;
import my.maleva.api.service.CustomerJobNotifyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customer-job-notifications")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class CustomerJobNotifyController {

    private final CustomerJobNotifyService service;

    public CustomerJobNotifyController(CustomerJobNotifyService service) {
        this.service = service;
    }

    @GetMapping
    public List<CustomerJobNotifyDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public CustomerJobNotifyDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<CustomerJobNotifyDto> create(@Valid @RequestBody CustomerJobNotifyDto dto) {
        CustomerJobNotifyDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/customer-job-notifications/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public CustomerJobNotifyDto update(@PathVariable Integer id, @Valid @RequestBody CustomerJobNotifyDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
