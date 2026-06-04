package my.maleva.api.module.master.controller;

import jakarta.annotation.security.PermitAll;
import my.maleva.api.module.master.dto.PaymentTermsMasterDto;
import my.maleva.api.module.master.service.PaymentTermsMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/payment-terms-master")
@Validated
@PermitAll
public class PaymentTermsMasterController {

    private final PaymentTermsMasterService service;

    public PaymentTermsMasterController(PaymentTermsMasterService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaymentTermsMasterDto> list() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    public PaymentTermsMasterDto get(@PathVariable Integer id) {
        return service.getById(id);
    }

    @PostMapping
    public ResponseEntity<PaymentTermsMasterDto> create(@Valid @RequestBody PaymentTermsMasterDto dto) {
        PaymentTermsMasterDto saved = service.create(dto);
        return ResponseEntity.created(URI.create("/api/payment-terms-master/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public PaymentTermsMasterDto update(@PathVariable Integer id, @Valid @RequestBody PaymentTermsMasterDto dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
