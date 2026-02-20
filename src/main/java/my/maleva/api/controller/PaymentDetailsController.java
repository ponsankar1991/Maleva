package my.maleva.api.controller;

import my.maleva.api.dto.PaymentDetailsDto;
import my.maleva.api.service.PaymentDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payment-details")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PaymentDetailsController {

    private final PaymentDetailsService service;

    public PaymentDetailsController(PaymentDetailsService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentDetailsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PaymentDetailsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PaymentDetailsDto> create(@Valid @RequestBody PaymentDetailsDto dto) {
        PaymentDetailsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/payment-details/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PaymentDetailsDto update(@PathVariable Integer id, @Valid @RequestBody PaymentDetailsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
