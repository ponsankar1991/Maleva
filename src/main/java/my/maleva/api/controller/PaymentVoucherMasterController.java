package my.maleva.api.controller;

import my.maleva.api.dto.PaymentVoucherMasterDto;
import my.maleva.api.service.PaymentVoucherMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payment-voucher-masters")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPRERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PaymentVoucherMasterController {

    private final PaymentVoucherMasterService service;

    public PaymentVoucherMasterController(PaymentVoucherMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentVoucherMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PaymentVoucherMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PaymentVoucherMasterDto> create(@Valid @RequestBody PaymentVoucherMasterDto dto) {
        PaymentVoucherMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/payment-voucher-masters/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PaymentVoucherMasterDto update(@PathVariable Integer id, @Valid @RequestBody PaymentVoucherMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
