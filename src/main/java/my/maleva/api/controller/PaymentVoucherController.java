package my.maleva.api.controller;

import my.maleva.api.dto.PaymentVoucherDto;
import my.maleva.api.service.PaymentVoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payment-vouchers")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PaymentVoucherController {

    private final PaymentVoucherService service;

    public PaymentVoucherController(PaymentVoucherService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentVoucherDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PaymentVoucherDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PaymentVoucherDto> create(@Valid @RequestBody PaymentVoucherDto dto) {
        PaymentVoucherDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/payment-vouchers/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PaymentVoucherDto update(@PathVariable Integer id, @Valid @RequestBody PaymentVoucherDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
