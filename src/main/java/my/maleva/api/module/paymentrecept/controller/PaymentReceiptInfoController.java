package my.maleva.api.module.paymentrecept.controller;

import my.maleva.api.module.paymentrecept.dto.PaymentReceiptInfoDto;
import my.maleva.api.module.paymentrecept.service.PaymentReceiptInfoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payment-receipt-infos")
@Validated
@PreAuthorize("hasAuthority('ROLE_SUPERADMIN') or hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_100')")
public class PaymentReceiptInfoController {

    private final PaymentReceiptInfoService service;

    public PaymentReceiptInfoController(PaymentReceiptInfoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentReceiptInfoDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PaymentReceiptInfoDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PaymentReceiptInfoDto> create(@Valid @RequestBody PaymentReceiptInfoDto dto) {
        PaymentReceiptInfoDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/payment-receipt-infos/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PaymentReceiptInfoDto update(@PathVariable Integer id, @Valid @RequestBody PaymentReceiptInfoDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
