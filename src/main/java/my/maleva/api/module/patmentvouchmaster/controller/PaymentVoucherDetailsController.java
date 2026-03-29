package my.maleva.api.module.patmentvouchmaster.controller;

import my.maleva.api.module.patmentvouchmaster.dto.PaymentVoucherDetailsDto;
import my.maleva.api.module.patmentvouchmaster.service.PaymentVoucherDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payment-voucher-details")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PaymentVoucherDetailsController {

    private final PaymentVoucherDetailsService service;

    public PaymentVoucherDetailsController(PaymentVoucherDetailsService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentVoucherDetailsDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PaymentVoucherDetailsDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PaymentVoucherDetailsDto> create(@Valid @RequestBody PaymentVoucherDetailsDto dto) {
        PaymentVoucherDetailsDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/payment-voucher-details/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PaymentVoucherDetailsDto update(@PathVariable Integer id, @Valid @RequestBody PaymentVoucherDetailsDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
